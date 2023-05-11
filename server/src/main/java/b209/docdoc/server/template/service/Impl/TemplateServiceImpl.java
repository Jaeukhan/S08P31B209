package b209.docdoc.server.template.service.Impl;

import b209.docdoc.server.config.utils.UUIDGenerator;
import b209.docdoc.server.email.service.EmailService;
import b209.docdoc.server.entity.*;
import b209.docdoc.server.exception.*;
import b209.docdoc.server.repository.*;
import b209.docdoc.server.template.dto.Request.DocumentTemplateSaveReqDTO;
import b209.docdoc.server.template.dto.Response.TemplateNameResDTO;
import b209.docdoc.server.template.dto.Response.TemplateResDTO;
import b209.docdoc.server.template.dto.Response.TemplatefileResDTO;
import b209.docdoc.server.template.dto.Response.WidgetResDTO;
import b209.docdoc.server.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private static final String METHOD_NAME = TemplateServiceImpl.class.getName();

    private final ResourceLoader resourceLoader;
    private final TemplateRepository templateRepository;
    private final TemplateFileRepository templateFileRepository;
    private final MemberRepository memberRepository;
    private final WidgetRepository widgetRepository;
    private final EmailService emailService;

    private final ReceiverRepository receiverRepository;

    @Value("${file.upload-dir}") // application.properties에 설정된 파일 업로드 디렉터리 경로
    private String uploadDir;

    public List<TemplateNameResDTO> getAllName(String memberEmail) {
        return templateRepository.findTemplatesByMemberEmail(memberEmail);
    }

    @Transactional
    public Object saveTemplate(MultipartFile pdfFile, DocumentTemplateSaveReqDTO documentTemplateSaveReqDTO, String fromEmail) throws Exception {
        Member member = memberRepository.findByMemberEmail(fromEmail).orElseThrow(() -> new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        String fileName = StringUtils.cleanPath(pdfFile.getOriginalFilename());
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        // 파일 확장자 확인 및 유효성 검사
        String[] allowedExtensions = new String[]{"pdf", "png", "jpg", "jpeg"};
        boolean isValidExtension = Arrays.stream(allowedExtensions).anyMatch(fileExtension::equalsIgnoreCase);
        if (!isValidExtension) {
            throw new InvalidFileExtensionException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        // 파일 저장 경로를 구합니다.
        Path memberDir = Paths.get(resourceLoader.getResource(uploadDir).getURI()).resolve(fromEmail);
        if (!Files.exists(memberDir)) {
            Files.createDirectories(memberDir);
        }

        //1. 파일 저장
        try {
            pdfFile.transferTo(memberDir.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new SaveFileException(ErrorCode.FOLDER_NOT_FOUND);
        }

        String uuid = UUIDGenerator.generateUUID(); // 템플릿 uuid


        //2. 문서파일 위치와 원본이름 DB 저장
        Templatefile templatefile = Templatefile.builder().
                templatefileOriginalName(fileName).
                templatefileSavedName(uuid).
                templatefileSavedPath(memberDir+"/"+uuid).
                build();
        templateFileRepository.save(templatefile);

        // 4. 사용자 위젯 생성 및 저장
        List<WidgetResDTO> templateWidget = documentTemplateSaveReqDTO.getWidgetResDTO();

        String templateDeadline = documentTemplateSaveReqDTO.getTemplateDeadline(); // 템플릿 마감일
        String templateName = documentTemplateSaveReqDTO.getTemplateName(); // 템플릿 이름

        // 3. 사용자 템플릿 생성 db 저장
        Template template = Template.builder().
                member(member)
                .templatefile(templatefile) // 템플릿 파일
                .templateUuid(uuid) // 템플릿 uuid
                .templateName(templateName) // 템플릿 파일 이름
                .templateIsDeleted(false) // 삭제 안함
                .templateDeadline(templateDeadline) // 마감일
                .build();
        templateRepository.save(template);

        List<Widget> templateWidgets = templateWidget.stream().map(widget ->
                        Widget.builder()
                                .templateIdx(template.getTemplateIdx()) // 템플릿 idx
                                .widgetName(widget.getWidgetType()) //  위젯 종류
                                .widgetSx(widget.getWidgetSx()) // 왼쪽위 x 좌표
                                .widgetDx(widget.getWidgetDx()) // 가로 길이
                                .widgetSy(widget.getWidgetSy()) // 왼쪽위 y 좌표
                                .widgetDy(widget.getWidgetDy()) // 세로 길이
                                .widgetType(widget.getWidgetType()) //  위젯 종류
                                .widgetContent(widget.getWidgetContent()) // 위젯 내용
                                .widgetIsBase(false) // 기본 위젯 여부
                                .widgetIsDeleted(false) // 삭제 여부
                                .build())
                .collect(Collectors.toList());
        widgetRepository.saveAll(templateWidgets);

        // 5. 수신자 이메일 전송 및 Receiver DB저장
        for (int i = 0; i < documentTemplateSaveReqDTO.getToName().size(); i++) { // 수신자들에게 템플릿 전송
            String toName = documentTemplateSaveReqDTO.getToName().get(i); // 수신자 이름
            String toEmail = documentTemplateSaveReqDTO.getToEmail().get(i); // 수신자 이메일

            //수신자가 회원가입한 멤버인지 확인
            boolean isMemmber = memberRepository.findByMemberEmail(toEmail).isPresent();

            Receiver receiver = Receiver.builder()
                    .template(template)
                    .templatefile(templatefile)
                    .receiverDocsName(documentTemplateSaveReqDTO.getTemplateName())
                    .receiverDeadline(templateDeadline)
                    .receiverIsMember(isMemmber) //수신자가 멤버 인지 확인
                    .receiverIsCompleted(false)// 문서 작성 완료 여부
                    .receiverSenderName(member.getMemberName())//발신자 이름
                    .receiverSenderEmail(fromEmail)//발신자 이메일
                    .receiverIsDeleted(false)// 문서 삭제 완료 여부
                    .receiverEmail(toEmail)//수신자 이메일
                    .build();

            receiverRepository.save(receiver);
            //이메일 전송
            emailService.sendTemplateMessage(uuid, toName, toEmail, fromEmail, templateDeadline, templateName); // 이메일로 템플릿 전송
        }
        return null;
    }

    @Transactional
    public TemplateResDTO getTemplateByMemberEmailAndTemplateIdx(String memberEmail, Long templateIdx) {
        Template template = templateRepository.findByMemberEmailAndTemplateIdx(memberEmail, templateIdx)
                .orElseThrow(() -> new TemplateNotFoundException(ErrorCode.TEMPLATE_NOT_FOUND));

        List<Widget> widgets = widgetRepository.findByTemplateIdx(templateIdx);

        List<WidgetResDTO> widgetDTOs = widgets.stream().map(widget ->
                new WidgetResDTO(
                        widget.getWidgetIdx(),
                        widget.getTemplateIdx(),
                        widget.getWidgetType(),
                        widget.getWidgetName(),
                        widget.getWidgetSx(),
                        widget.getWidgetSy(),
                        widget.getWidgetDx(),
                        widget.getWidgetDy(),
                        widget.getWidgetContent(),
                        widget.getWidgetIsBase(),
                        widget.getWidgetIsDeleted()
                )
        ).collect(Collectors.toList());

        Templatefile templatefile = template.getTemplatefile();

        TemplatefileResDTO templatefileDTO = new TemplatefileResDTO(
                templatefile.getTemplatefileIdx(),
                templatefile.getTemplatefileOriginalName(),
                templatefile.getTemplatefileSavedName(),
                templatefile.getTemplatefileSavedPath()
        );

        return new TemplateResDTO(templateIdx, widgetDTOs, templatefileDTO);
    }
}
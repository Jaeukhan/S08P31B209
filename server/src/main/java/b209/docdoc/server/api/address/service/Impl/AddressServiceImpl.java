package b209.docdoc.server.api.address.service.Impl;

import b209.docdoc.server.api.address.dto.AddressInfo;
import b209.docdoc.server.api.address.dto.AddressInfoSimple;
import b209.docdoc.server.api.address.dto.Request.AddressEditorReq;
import b209.docdoc.server.api.address.dto.Request.AddressRegisterReq;
import b209.docdoc.server.api.address.dto.Response.AddressListRes;
import b209.docdoc.server.api.address.service.AddressService;
import b209.docdoc.server.config.security.auth.MemberDTO;
import b209.docdoc.server.domain.entity.AddressBook;
import b209.docdoc.server.domain.entity.Member;
import b209.docdoc.server.domain.repository.AddressBookRepository;
import b209.docdoc.server.domain.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {
    private MemberRepository memberRepository;
    private AddressBookRepository addressBookRepository;

    private final String NO_GROUP = "-";
    private final String NO_POSITION = "-";
    private final String NO_PHONE = "-";
    private final String NO_GROUP_KO = "그룹없음";

    private HashSet<String> getMemberAddressEmailSet(String memberEmail) {
        Optional<Member> member = memberRepository.findByMemberEmail(memberEmail);
        if (member.isEmpty()) return null;

        List<AddressBook> list = addressBookRepository.findAllByMemberAndAddressIsDeleted(member.get(), false);
        HashSet<String> results = new HashSet<String>();
        for (AddressBook address: list) {
            results.add(address.getAddresEmail());
        }

        return results;
    }

    @Override
    public String saveOneAddress(AddressRegisterReq req, MemberDTO member) {
        if (member.getIsDeleted()) return null;

        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;

        addressBookRepository.save(
                AddressBook.builder()
                        .member(memberObj.get())
                        .addressName(req.getName())
                        .addresEmail(req.getEmail())
                        .addressPhone((req.getPhone() == null || req.getPhone().trim().length() == 0) ? NO_PHONE : req.getPhone())
                        .addressGroup((req.getGroup() == null || req.getGroup().trim().length() == 0) ? NO_GROUP : req.getGroup())
                        .addressPosition((req.getPosition() == null || req.getPosition().trim().length() == 0) ? NO_POSITION : req.getPosition())
                        .addressIsDeleted(false)
                        .build()
        );

        return null;
    }

    @Override
    public AddressListRes getAddressListByGroup(String group, MemberDTO member) {
        List<AddressBook> list = new ArrayList<>();
        List<AddressInfo> result = new ArrayList<>();

        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;

        if (group.equals("전체그룹")) list = addressBookRepository.findAllByMemberAndAddressIsDeleted(memberObj.get(), false);
        else if (group.equals(NO_GROUP_KO)) list = addressBookRepository.findAllByMemberAndAddressGroupAndAddressIsDeleted(memberObj.get(), NO_GROUP, false);
        else list = addressBookRepository.findAllByMemberAndAddressGroupAndAddressIsDeleted(memberObj.get(), group, false);

        for (AddressBook address: list) {
            result.add(new AddressInfo(
                    address.getAddressIdx(),
                    address.getAddressName(),
                    address.getAddresEmail(),
                    address.getAddressPhone(),
                    address.getAddressGroup(),
                    address.getAddressPosition()
            ));
        }

        List<String> groups = addressBookRepository.findAllGroups();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(NO_GROUP)) {
                groups.set(i, NO_GROUP_KO);
                break;
            }
        }

        return AddressListRes.of(groups, result);
    }

    @Override
    public String removeAddress(String addressIdx, MemberDTO member) {
        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;

        AddressBook addressBook = addressBookRepository.findByAddressIdx(Long.parseLong(addressIdx));
        if (addressBook == null) return null;

        addressBook.setAddressIsDeleted(true);
        addressBookRepository.save(addressBook);

        return null;
    }

    @Override
    public String removeAddressByGroup(String group, MemberDTO member) {
        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;
        List<AddressBook> addressBooks = new ArrayList<AddressBook>();

        if (group.equals(NO_GROUP_KO)) addressBooks = addressBookRepository.findAllByMemberAndAddressGroupAndAddressIsDeleted(memberObj.get(), NO_GROUP, false);
        else addressBooks = addressBookRepository.findAllByMemberAndAddressGroupAndAddressIsDeleted(memberObj.get(), group, false);

        if (addressBooks == null || addressBooks.size() == 0) return null;

        for (AddressBook addressBook: addressBooks) {
            addressBook.setAddressIsDeleted(true);
            addressBookRepository.save(addressBook);
        }

        return null;
    }

    @Override
    public AddressListRes getAddressEditorListByName(String name, MemberDTO member) {
        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;

        List<AddressBook> list = addressBookRepository.findAllByMemberAndAddressNameStartingWithAndAddressIsDeleted(memberObj.get(), name, false);
        List<AddressInfo> result = new ArrayList<>();

        for (AddressBook address: list) {
            result.add(new AddressInfo(
                    address.getAddressIdx(),
                    address.getAddressName(),
                    address.getAddresEmail(),
                    address.getAddressPhone(),
                    address.getAddressGroup(),
                    address.getAddressPosition()
            ));
        }

        return AddressListRes.of(result);
    }

    @Override
    public String saveAddressEditor(AddressEditorReq req, MemberDTO member) {

        Optional<Member> memberObj = memberRepository.findByMemberEmail(member.getEmail());
        if (memberObj.isEmpty()) return null;

        HashSet<String> emails = getMemberAddressEmailSet(member.getEmail());

        for (AddressInfoSimple address: req.getAddresses()) {
            if (emails != null && address.getEmail() != null && !emails.contains(address.getEmail())) {
                addressBookRepository.save(
                        AddressBook.builder()
                                .member(memberObj.get())
                                .addressName(address.getName())
                                .addresEmail(address.getEmail())
                                .addressPhone((address.getPhone() == null || address.getPhone().trim().length() == 0) ? NO_PHONE : address.getPhone())
                                .addressGroup((address.getGroup() == null || address.getGroup().trim().length() == 0) ? NO_GROUP : address.getGroup())
                                .addressPosition((address.getPosition() == null || address.getPosition().trim().length() == 0) ? NO_POSITION : address.getPosition())
                                .addressIsDeleted(false)
                                .build()
                );
            }
        }

        return null;
    }
}

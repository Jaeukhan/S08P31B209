package b209.docdoc.server.repository;

import b209.docdoc.server.entity.Template;
import b209.docdoc.server.template.dto.Response.TemplateNameResDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {


	@Query("SELECT new b209.docdoc.server.template.dto.Response.TemplateNameResDTO(t.templateIdx, t.templateName) FROM Template t JOIN t.member m WHERE m.memberEmail = :memberEmail")
	List<TemplateNameResDTO> findTemplatesByMemberEmail(@Param("memberEmail") String memberEmail);




}

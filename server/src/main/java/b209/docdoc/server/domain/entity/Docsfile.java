package b209.docdoc.server.domain.entity;

import b209.docdoc.server.config.utils.BaseDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "docsfile")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Docsfile extends BaseDateTime implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "docsfile_idx")
    private Long docsfileIdx;

    @NotBlank
    @Column(length = 50)
    private String docsfileOriginalName; // 문서 name

    @NotBlank
    @Column(length = 255)
    private String docsfileSavedName; // 문서 name

    @NotBlank
    @Column(length = 255)
    private String docsfileSavedPath; // 문서 path

    @NotBlank
    @Column(length = 255)
    private String uuid; // 템플릿 uuid

    @NotBlank
    @Column(length = 50)
    private String toEmail; // 수신자 email;

    @NotBlank
    @Column(length = 50)
    private String fromEmail; // 발신자 email;

    @Override
    public void prePersist() {
        super.prePersist();
    }
}
package spring.ai.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VectorMetadataDto {
    private String repo_name;
    private String apid;
    private String msgid;
    private String description;
    private String owner;
    private String platform;
    private String type;

}

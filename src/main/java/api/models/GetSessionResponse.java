package api.models;

import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetSessionResponse extends BaseModel {

    private List<UserDto> results;

    @Data
    public static class UserDto {
        private String uuid;
        private String display;
        private String username;
        private String systemId;
        private Map<String, String> userProperties;
        private PersonDto person;
        private List<PrivilegeDto> privileges;
        private List<RoleDto> roles;
        private boolean retired;
        private List<LinkDto> links;
        private String resourceVersion;
    }

    @Data
    public static class PersonDto {
        private String uuid;
        private String display;
        private List<LinkDto> links;
    }

    @Data
    public static class PrivilegeDto {
        private String uuid;
        private String display;
        private List<LinkDto> links;
    }

    @Data
    public static class RoleDto {
        private String uuid;
        private String display;
        private List<LinkDto> links;
    }

    @Data
    public static class LinkDto {
        private String rel;
        private String uri;
    }
}

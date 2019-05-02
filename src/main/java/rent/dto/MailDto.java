package rent.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MailDto {
    private String from;
    private String to;
    private String subject;
    private Map<String, Object> model;
}

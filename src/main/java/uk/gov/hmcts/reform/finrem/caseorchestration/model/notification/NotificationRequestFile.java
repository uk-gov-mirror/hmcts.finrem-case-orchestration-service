package uk.gov.hmcts.reform.finrem.caseorchestration.model.notification;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class NotificationRequestFile {
    String templatePlaceholder;
    @ToString.Exclude
    byte[] fileContents;
    String filename;
}

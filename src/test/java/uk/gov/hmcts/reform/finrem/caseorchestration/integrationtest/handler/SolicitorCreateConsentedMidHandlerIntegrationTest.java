package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SolicitorCreateConsentedMidHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class SolicitorCreateConsentedMidHandlerIntegrationTest extends BaseTest {

    @Autowired
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitorCreateConsentedMidHandler solicitorCreateConsentedMidHandler;

    @Test
    public void shouldSetToDefault() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = solicitorCreateConsentedMidHandler.handle(CallbackRequest.builder()
            .eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>())
                .build())
            .build(),"");
        Assert.assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        Assert.assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, response.getData().get(CV_LOWERCASE_LABEL_FIELD));
        Assert.assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, response.getData().get(CV_OTHER_DOC_LABEL_FIELD));

        //Assert.assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        //Assert.assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, response.getData().get(CV_LOWERCASE_LABEL_FIELD));
        //Assert.assertEquals(CV_OTHER_DOC_LABEL_VALUE, response.getData().get(CV_OTHER_DOC_LABEL_FIELD));
    }
}

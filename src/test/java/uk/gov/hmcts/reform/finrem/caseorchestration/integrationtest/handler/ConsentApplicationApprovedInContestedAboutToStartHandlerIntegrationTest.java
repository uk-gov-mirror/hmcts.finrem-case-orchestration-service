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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.ConsentApplicationApprovedInContestedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class ConsentApplicationApprovedInContestedAboutToStartHandlerIntegrationTest extends BaseTest {

    @Autowired
    private IdamService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsentApplicationApprovedInContestedAboutToStartHandler consentApplicationApprovedInContestedAboutToStartHandler;

    @Test
    public void setTheLoggedInUserIfNull() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = consentApplicationApprovedInContestedAboutToStartHandler.handle(CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(123L)
                .data(new HashMap<>())
                .build())
            .build(),"");
        Assert.assertEquals(CONTESTED_ORDER_DIRECTION_JUDGE_NAME, service.getIdamFullName(""));
    }

}

package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SolicitorCreateContestedMidHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedChildrenService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class SolicitorCreateContestedMidHandlerIntegrationest extends BaseTest {

    @Autowired
    private ContestedChildrenService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitorCreateContestedMidHandler solicitorCreateContestedMidHandler;

    @Test
    public void shouldSetToDefault() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = solicitorCreateContestedMidHandler.handle(CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>())
                .build())
            .build(),"");
        assertEquals(0, response.getErrors().size());
    }

}

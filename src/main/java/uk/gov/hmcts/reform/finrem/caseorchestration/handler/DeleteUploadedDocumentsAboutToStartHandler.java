package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class DeleteUploadedDocumentsAboutToStartHandler extends FinremCallbackHandler {

    public DeleteUploadedDocumentsAboutToStartHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DELETE_UPLOADED_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        caseData.setUploadedDocumentsToDelete(null);
        //retrieve documents from uploaded document individual fields
        List<Optional<CaseDocument>> singleUploadedCaseDocuments = getSingleUploadedCaseDocuments(caseData);
        List<String> documentTypes = getUploadedDocumentTypes();

        List<DynamicMultiSelectListElement> listElements = new ArrayList<>();

        addSingleUploadedCaseDocumentsToListElements(singleUploadedCaseDocuments, documentTypes, listElements);

        //retrieve documents from uploaded document collection fields
        List<CaseDocument> hearingOrderOtherDocuments = getHearingOrderOtherDocuments(caseData);
        List<CaseDocument> additionalCicDocuments = getAdditionalCicDocuments(caseData);
        List<CaseDocument> generalApplicationDirectionsDocuments = getGeneralApplicationDocuments(caseData, "generalApplicationDirectionsDocumentCollection");
        List<CaseDocument> generalApplicationDocuments = getGeneralApplicationDocuments(caseData, "generalApplicationDocumentCollection");
        List<CaseDocument> generalApplicationDraftOrders = getGeneralApplicationDocuments(caseData, "generalApplicationDraftOrderCollection");
        List<CaseDocument> interimUploadAdditionalDocuments = getInterimUploadAdditionalDocuments(caseData);
        List<CaseDocument> generalEmailUploadedDocuments = getGeneralEmailUploadedDocuments(caseData);
        List<CaseDocument> uploadAdditionalDocuments = getUploadAdditionalDocuments(caseData);
        List<CaseDocument> copyOfPaperFormADocuments = getCopyOfPaperFormADocuments(caseData);
        List<CaseDocument> draftDirectionOrderCollection = getDraftDirectionOrderCollectionDocuments(caseData);
        List<CaseDocument> uploadedDocuments = getUploadedDocuments(caseData);
        List<CaseDocument> scannedDocuments = getScannedDocuments(caseData);
        List<CaseDocument> pensionDocuments = getPensionDocuments(caseData);
        List<CaseDocument> hearingBundleDocuments = getHearingBundleDocuments(caseData);

        addCollectionToListElements(hearingOrderOtherDocuments, listElements, "hearingOrderOtherDocument");
        addCollectionToListElements(additionalCicDocuments, listElements, "additionalCicDocument");
        addCollectionToListElements(generalApplicationDraftOrders, listElements, "generalApplicationDraftOrderCollection");
        addCollectionToListElements(generalApplicationDocuments, listElements, "generalApplicationDocumentCollection");
        addCollectionToListElements(generalApplicationDirectionsDocuments, listElements, "generalApplicationDirectionsDocumentCollection");
        addCollectionToListElements(interimUploadAdditionalDocuments, listElements, "interimUploadAdditionalDocumentCollection");
        addCollectionToListElements(generalEmailUploadedDocuments, listElements, "generalEmailUploadedDocumentCollection");
        addCollectionToListElements(uploadAdditionalDocuments, listElements, "uploadAdditionalDocumentCollection");
        addCollectionToListElements(copyOfPaperFormADocuments, listElements, "copyOfPaperFormADocumentCollection");
        addCollectionToListElements(draftDirectionOrderCollection, listElements, "draftDirectionOrderDocumentCollection");
        addCollectionToListElements(uploadedDocuments, listElements, "uploadedDocumentCollection");
        addCollectionToListElements(scannedDocuments, listElements, "scannedDocumentCollection");
        addCollectionToListElements(pensionDocuments, listElements, "pensionDocumentCollection");
        addCollectionToListElements(hearingBundleDocuments, listElements, "hearingBundleDocumentCollection");
        DynamicMultiSelectList dynamicList = DynamicMultiSelectList.builder().listItems(listElements).build();
        caseData.setUploadedDocumentsToDelete(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void addCollectionToListElements(List<CaseDocument> caseDocuments,
                                             List<DynamicMultiSelectListElement> listElements,
                                             String codePrefix) {
        AtomicInteger count = new AtomicInteger();
        caseDocuments.forEach(document -> {
            if (document != null) {
            {
                count.addAndGet(1);
                DynamicMultiSelectListElement dynamicMultiSelectListElement =
                    DynamicMultiSelectListElement.builder()
                        .code(codePrefix + "-" + count)
                        .label(document.getDocumentFilename()).build();
                listElements.add(dynamicMultiSelectListElement);
            };
        }});
    }

    private List<CaseDocument>  getAdditionalCicDocuments(FinremCaseData caseData) {
        return getCaseDocuments(caseData.getAdditionalCicDocuments());
    }

    private List<CaseDocument> getHearingOrderOtherDocuments(FinremCaseData caseData) {
        return getCaseDocuments(caseData.getHearingOrderOtherDocuments());
    }

    private static List<CaseDocument> getCaseDocuments(List<DocumentCollection> collection) {
        List<CaseDocument> documents = new ArrayList<>();
        if (collection != null && !collection.isEmpty()) {
            collection.forEach(
                caseDocument ->
                    documents.add(caseDocument.getValue()));
        }
        return documents;
    }

    private List<CaseDocument> getInterimUploadAdditionalDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getInterimWrapper().getInterimHearings() != null
            && !caseData.getInterimWrapper().getInterimHearings().isEmpty()) {
            caseData.getInterimWrapper().getInterimHearings().forEach(
                caseDocument -> documents.add(caseDocument.getValue().getInterimUploadAdditionalDocument()));
        }
        return documents;
    }

    private List<CaseDocument> getUploadAdditionalDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getUploadAdditionalDocument() != null) {
            caseData.getUploadAdditionalDocument().forEach(
                caseDocument -> documents.add(caseDocument.getValue().getAdditionalDocuments()));
        }
        return documents;
    }

    private List<CaseDocument> getGeneralEmailUploadedDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getGeneralEmailWrapper().getGeneralEmailCollection() != null
            && !caseData.getGeneralEmailWrapper().getGeneralEmailCollection().isEmpty()) {
            caseData.getGeneralEmailWrapper().getGeneralEmailCollection().forEach(
                caseDocument -> documents.add(caseDocument.getValue().getGeneralEmailUploadedDocument()));
        }
        return documents;
    }

    private List<CaseDocument> getCopyOfPaperFormADocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getCopyOfPaperFormA() != null
            && !caseData.getCopyOfPaperFormA().isEmpty()) {
            caseData.getCopyOfPaperFormA().forEach(
                caseDocument -> documents.add(caseDocument.getValue().getUploadedDocument()));
        }
        return documents;
    }

    private List<CaseDocument> getDraftDirectionOrderCollectionDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection() != null
            && !caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection().isEmpty()) {
            caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection().forEach(
                caseDocument -> documents.add(caseDocument.getValue().getUploadDraftDocument()));
        }
        return documents;
    }

    private List<CaseDocument> getUploadedDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getUploadGeneralDocuments() != null
            && !caseData.getUploadGeneralDocuments().isEmpty()) {
            caseData.getUploadGeneralDocuments().forEach(
                caseDocument -> documents.add(CaseDocument.builder()
                    .documentFilename(caseDocument.getValue().getDocumentLink().getDocumentFilename()).build()));
        }
        return documents;
    }

    private List<CaseDocument> getScannedDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getScannedDocuments() != null
            && !caseData.getScannedDocuments().isEmpty()) {
            caseData.getScannedDocuments().forEach(
                caseDocument -> documents.add(CaseDocument.builder()
                    .documentFilename(caseDocument.getValue().getUrl().getDocumentFilename()).build()));
        }
        return documents;
    }

    private List<CaseDocument> getPensionDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders() != null
            && !caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders().isEmpty()) {
            caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders().forEach(
                order -> order.getApprovedOrder().getPensionDocuments().forEach(pensionCollection -> documents.add(CaseDocument.builder()
                    .documentFilename(pensionCollection.getTypedCaseDocument().getPensionDocument().getDocumentFilename()).build())));
        }
        return documents;
    }


    private List<CaseDocument> getHearingBundleDocuments(FinremCaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getHearingUploadBundle() != null
            && !caseData.getHearingUploadBundle().isEmpty()) {
            caseData.getHearingUploadBundle().forEach(
                bundle ->
                    bundle.getValue().getHearingBundleDocuments().forEach(document ->
                        documents.add(document.getValue().getBundleDocuments())
                ));
        }
        return documents;
    }

    private List<CaseDocument> getGeneralApplicationDocuments(FinremCaseData caseData, String collectionName) {
        List<CaseDocument> documents = new ArrayList<>();
        if (caseData.getGeneralApplicationWrapper().getGeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getGeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
                caseDocument -> {
                    switch (collectionName) {
                        case "generalApplicationDocumentCollection" -> documents.add(
                                caseDocument.getValue().getGeneralApplicationDocument());
                        case "generalApplicationDirectionsDocumentCollection" -> documents.add(
                                caseDocument.getValue().getGeneralApplicationDirectionsDocument());
                        case "generalApplicationDraftOrderCollection" -> documents.add(
                                caseDocument.getValue().getGeneralApplicationDraftOrder());
                    }
            });
        }
        return documents;
    }

    private static void addSingleUploadedCaseDocumentsToListElements(List<Optional<CaseDocument>> singleUploadedCaseDocuments,
                                                                     List<String> documentTypes,
                                                                     List<DynamicMultiSelectListElement> listElements) {
        singleUploadedCaseDocuments.forEach(caseDocument -> {
            if (caseDocument.isPresent()) {
                int index = singleUploadedCaseDocuments.indexOf(caseDocument);
                DynamicMultiSelectListElement dynamicMultiSelectListElement =
                    DynamicMultiSelectListElement.builder()
                        .code(documentTypes.get(index))
                        .label(caseDocument.get().getDocumentFilename()).build();
                listElements.add(dynamicMultiSelectListElement);
            }
        });
    }

    private List<Optional<CaseDocument>> getSingleUploadedCaseDocuments(FinremCaseData caseData) {
        return new ArrayList<>(
            Arrays.asList(Optional.ofNullable(caseData.getDivorceUploadEvidence1()),
                Optional.ofNullable(caseData.getDivorceUploadEvidence2()),
                Optional.ofNullable(caseData.getDivorceUploadPetition()),
                Optional.ofNullable(caseData.getVariationOrderDocument()),
                Optional.ofNullable(caseData.getAdditionalListOfHearingDocuments()),
                Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument()),
                Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument()),
                Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder()),
                Optional.ofNullable(caseData.getConsentVariationOrderDocument()),
                Optional.ofNullable(caseData.getAdditionalDocument()),
                Optional.ofNullable(caseData.getRefusalOrderPreviewDocument()),
                Optional.ofNullable(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument()),
                Optional.ofNullable(caseData.getConsentOrder()),
                Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Joint()),
                Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Applicant()),
                Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Respondent()),
                Optional.ofNullable(caseData.getOrderRefusalPreviewDocument()),
                Optional.ofNullable(caseData.getInterimWrapper().getInterimUploadAdditionalDocument()),
                Optional.ofNullable(caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument())));
    }

    private List<String> getUploadedDocumentTypes() {
        return List.of("divorceUploadEvidence1", "divorceUploadEvidence2",
            "divorceUploadPetition", "variationOrderDocument", "additionalListOfHearingDocuments",
            "generalApplicationDirectionsDocument", "generalApplicationDocument", "generalApplicationDraftOrder",
            "consentVariationOrderDocument", "additionalDocument", "refusalOrderPreviewDocument",
            "generalOrderPreviewDocument", "consentOrder", "consentD81Joint", "consentD81Applicant",
            "consentD81Respondent", "orderRefusalPreviewDocument", "interimUploadAdditionalDocument",
            "generalEmailUploadedDocument"
        );
    }
}
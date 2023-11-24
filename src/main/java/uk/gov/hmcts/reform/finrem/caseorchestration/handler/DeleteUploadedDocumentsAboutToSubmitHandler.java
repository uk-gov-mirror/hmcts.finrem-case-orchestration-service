package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DeleteUploadedDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

    @Autowired
    public DeleteUploadedDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType) && CaseType.CONTESTED.equals(caseType)
            && EventType.DELETE_UPLOADED_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<Pair<String, String>> documentsToDelete = new ArrayList<>();

        DynamicMultiSelectList selectedDocumentsForDeletion = caseData.getUploadedDocumentsToDelete();
        selectedDocumentsForDeletion.getValue().forEach(dynamicMultiSelectListElement ->
            documentsToDelete.add(Pair.of(dynamicMultiSelectListElement.getCode(), dynamicMultiSelectListElement.getLabel())));
        List<String> errors = new ArrayList<>();
        documentsToDelete.forEach(document -> deleteDocuments(document, caseData, errors));

        caseData.setUploadedDocumentsToDelete(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void deleteDocuments(Pair<String, String> document, FinremCaseData caseData, List<String> errors) {
        if (document.getLeft().equals("divorceUploadEvidence1")) {
            caseData.setDivorceUploadEvidence1(null);
        } else if (document.getLeft().equals("divorceUploadEvidence2")) {
            caseData.setDivorceUploadEvidence2(null);
        } else if (document.getLeft().equals("divorceUploadPetition")) {
            caseData.setDivorceUploadPetition(null);
        } else if (document.getLeft().equals("variationOrderDocument")) {
            caseData.setVariationOrderDocument(null);
        } else if (document.getLeft().equals("additionalListOfHearingDocuments")) {
            caseData.setAdditionalListOfHearingDocuments(null);
        } else if (document.getLeft().equals("generalApplicationDirectionsDocument")) {
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsDocument(null);
        } else if (document.getLeft().equals("generalApplicationDocument")) {
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDocument(null);
        } else if (document.getLeft().equals("generalApplicationDraftOrder")) {
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDraftOrder(null);
        } else if (document.getLeft().equals("consentVariationOrderDocument")) {
            caseData.setConsentVariationOrderDocument(null);
        } else if (document.getLeft().equals("refusalOrderPreviewDocument")) {
            caseData.setRefusalOrderPreviewDocument(null);
        } else if (document.getLeft().equals("generalOrderPreviewDocument")) {
            caseData.getGeneralOrderWrapper().setGeneralOrderPreviewDocument(null);
        } else if (document.getLeft().equals("consentOrder")) {
            caseData.setConsentOrder(null);
        } else if (document.getLeft().equals("consentD81Joint")) {
            caseData.getConsentOrderWrapper().setConsentD81Joint(null);
        } else if (document.getLeft().equals("consentD81Applicant")) {
            caseData.getConsentOrderWrapper().setConsentD81Applicant(null);
        } else if (document.getLeft().equals("consentD81Respondent")) {
            caseData.getConsentOrderWrapper().setConsentD81Respondent(null);
        } else if (document.getLeft().equals("orderRefusalPreviewDocument")) {
            caseData.setOrderRefusalPreviewDocument(null);
        } else if (document.getLeft().equals("interimUploadAdditionalDocument")) {
            caseData.getInterimWrapper().setInterimUploadAdditionalDocument(null);
        } else if (document.getLeft().equals("generalEmailUploadedDocument")) {
            caseData.getGeneralEmailWrapper().setGeneralEmailUploadedDocument(null);
        } else if (document.getLeft().startsWith("hearingOrderOtherDocument")) {
            caseData.getHearingOrderOtherDocuments().removeIf(doc ->
                doc.getValue().getDocumentFilename().equals(document.getRight()));
        } else if (document.getLeft().startsWith("additionalCicDocument")) {
            caseData.getAdditionalCicDocuments().removeIf(doc ->
                doc.getValue().getDocumentFilename().equals(document.getRight()));
            List<ConsentInContestedApprovedOrderCollection> appColl = caseData.getConsentOrderWrapper().getAppConsentApprovedOrders();
            List<ConsentInContestedApprovedOrderCollection> respColl = caseData.getConsentOrderWrapper().getRespConsentApprovedOrders();
            List<ConsentInContestedApprovedOrderCollection> intv1Coll = caseData.getConsentOrderWrapper().getIntv1ConsentApprovedOrders();
            List<ConsentInContestedApprovedOrderCollection> intv2Coll = caseData.getConsentOrderWrapper().getIntv2ConsentApprovedOrders();
            List<ConsentInContestedApprovedOrderCollection> intv3Coll = caseData.getConsentOrderWrapper().getIntv3ConsentApprovedOrders();
            List<ConsentInContestedApprovedOrderCollection> intv4Coll = caseData.getConsentOrderWrapper().getIntv4ConsentApprovedOrders();
            removeAdditionalCicDocumentsFromCollections(document, appColl);
            removeAdditionalCicDocumentsFromCollections(document, respColl);
            removeAdditionalCicDocumentsFromCollections(document, intv1Coll);
            removeAdditionalCicDocumentsFromCollections(document, intv2Coll);
            removeAdditionalCicDocumentsFromCollections(document, intv3Coll);
            removeAdditionalCicDocumentsFromCollections(document, intv4Coll);
        } else if (document.getLeft().startsWith("generalApplicationDirectionsDocumentCollection")) {
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(generalApplication -> {
                if (generalApplication != null && generalApplication.getValue() != null
                    && generalApplication.getValue().getGeneralApplicationDocument() != null) {
                    if (generalApplication.getValue().getGeneralApplicationDirectionsDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        generalApplication.getValue().setGeneralApplicationDirectionsDocument(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("generalApplicationDraftOrderCollection")) {
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(generalApplication -> {
                if (generalApplication != null && generalApplication.getValue() != null
                    && generalApplication.getValue().getGeneralApplicationDocument() != null) {
                    if (generalApplication.getValue().getGeneralApplicationDraftOrder()
                        .getDocumentFilename().equals(document.getRight())) {
                        generalApplication.getValue().setGeneralApplicationDraftOrder(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("generalApplicationDocumentCollection")) {
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(generalApplication -> {
                if (generalApplication != null && generalApplication.getValue() != null
                    && generalApplication.getValue().getGeneralApplicationDocument() != null) {
                    if (generalApplication.getValue().getGeneralApplicationDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        generalApplication.getValue().setGeneralApplicationDocument(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("interimUploadAdditionalDocumentCollection")) {
            caseData.getInterimWrapper().getInterimHearings().forEach(interimHearing -> {
                if (interimHearing != null && interimHearing.getValue() != null
                    && interimHearing.getValue().getInterimUploadAdditionalDocument() != null) {
                    if (interimHearing.getValue().getInterimUploadAdditionalDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        interimHearing.getValue().setInterimUploadAdditionalDocument(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("generalEmailUploadedDocumentCollection")) {
            caseData.getGeneralEmailWrapper().getGeneralEmailCollection().forEach(generalEmailCollection -> {
                if (generalEmailCollection != null && generalEmailCollection.getValue() != null
                    && generalEmailCollection.getValue().getGeneralEmailUploadedDocument() != null) {
                    if (generalEmailCollection.getValue().getGeneralEmailUploadedDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        generalEmailCollection.getValue().setGeneralEmailUploadedDocument(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("uploadAdditionalDocumentCollection")) {
            caseData.getUploadAdditionalDocument().forEach(additionalDocument -> {
                if (additionalDocument != null && additionalDocument.getValue() != null
                    && additionalDocument.getValue().getAdditionalDocuments() != null) {
                    if (additionalDocument.getValue().getAdditionalDocuments()
                        .getDocumentFilename().equals(document.getRight())) {
                        additionalDocument.getValue().setAdditionalDocuments(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("copyOfPaperFormADocumentCollection")) {
            caseData.getCopyOfPaperFormA().forEach(copyOfPaperFormA -> {
                if (copyOfPaperFormA != null && copyOfPaperFormA.getValue() != null
                    && copyOfPaperFormA.getValue().getUploadedDocument() != null) {
                    if (copyOfPaperFormA.getValue().getUploadedDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        copyOfPaperFormA.getValue().setUploadedDocument(null);
                    }
                }
            });
        } else if (document.getLeft().startsWith("draftDirectionOrderDocumentCollection")) {
            caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection().forEach(draftDirectionOrder -> {
                if (draftDirectionOrder != null && draftDirectionOrder.getValue() != null
                    && draftDirectionOrder.getValue().getUploadDraftDocument() != null) {
                    if (draftDirectionOrder.getValue().getUploadDraftDocument()
                        .getDocumentFilename().equals(document.getRight())) {
                        draftDirectionOrder.getValue().setUploadDraftDocument(null);
                    }
                }
            });//TODO: delete other related collections where copied across for draftDirectionOrder
        } else if (document.getLeft().startsWith("uploadedDocumentCollection")) {
            List<UploadGeneralDocumentCollection> uploadedDocuments = new ArrayList<>();
            for (UploadGeneralDocumentCollection uploadedDocument : caseData.getUploadGeneralDocuments()) {
                if (uploadedDocument != null && uploadedDocument.getValue() != null) {
                    if (!uploadedDocument.getValue().getDocumentLink().getDocumentFilename().equals(document.getRight())) {
                        uploadedDocuments.add(uploadedDocument);
                    }
                }
            }
            caseData.setUploadGeneralDocuments(uploadedDocuments);//TODO: define this code
        } else if (document.getLeft().startsWith("scannedDocumentCollection")) {
            List<ScannedDocumentCollection> scannedDocuments = new ArrayList<>();
            for (ScannedDocumentCollection scannedDocument : caseData.getScannedDocuments()) {
                if (scannedDocument != null && scannedDocument.getValue() != null) {
                    if (!scannedDocument.getValue().getUrl().getDocumentFilename().equals(document.getRight())) {
                        scannedDocuments.add(scannedDocument);
                    }
                }
            }
            caseData.setScannedDocuments(scannedDocuments);
        } else if (document.getLeft().startsWith("pensionDocumentCollection")) {
            caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders().forEach(order -> {
                List<PensionTypeCollection> pensionDocs = order.getApprovedOrder().getPensionDocuments();
                if (pensionDocs != null
                    && !pensionDocs.isEmpty()) {
                    pensionDocs.removeIf(doc ->
                        doc.getTypedCaseDocument().getPensionDocument().getDocumentFilename().equals(document.getRight()));
                }
            });
        } else if (document.getLeft().startsWith("hearingBundleDocumentCollection")) {
            caseData.getHearingUploadBundle().forEach(bundle -> {
                List<HearingBundleDocumentCollection> documents = bundle.getValue().getHearingBundleDocuments();
                if (documents != null
                    && !documents.isEmpty()) {
                    documents.removeIf(doc ->
                        doc.getValue().getBundleDocuments().getDocumentFilename().equals(document.getRight()));
                }
            });
        }
        //add applicant and respondent scanned docs collections
        else {
            errors.add("The document type " + document.getLeft() + " of the file named " + document.getRight() + " was not recognised.");
        }
    }

    private static void removeAdditionalCicDocumentsFromCollections(Pair<String, String> document,
                                                                    List<ConsentInContestedApprovedOrderCollection> collections) {
        if (collections != null &&
            !collections.isEmpty()) {
            collections.forEach(order -> {
                if (order != null && order.getApprovedOrder() != null
                    && order.getApprovedOrder().getAdditionalConsentDocuments() != null) {
                    order.getApprovedOrder().getAdditionalConsentDocuments().removeIf(doc ->
                        doc.getValue().getDocumentFilename().equals(document.getRight()));
                }
            });
        }
    }
}
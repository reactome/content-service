package org.reactome.server.orcid.controller;

import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.domain.result.SimpleEventProjection;
import org.reactome.server.graph.service.PersonService;
import org.reactome.server.orcid.dao.OrcidReportDAO;
import org.reactome.server.orcid.domain.ClaimingSummary;
import org.reactome.server.orcid.domain.OrcidToken;
import org.reactome.server.orcid.domain.WorkBulkResponse;
import org.reactome.server.orcid.domain.Works;
import org.reactome.server.orcid.exception.OrcidOAuthException;
import org.reactome.server.orcid.exception.WorkClaimException;
import org.reactome.server.orcid.util.OrcidHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.reactome.server.orcid.util.OrcidHelper.ContributionRole;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Controller
@RequestMapping("/orcid")
public class OrcidController {

    private PersonService personService;
    private OrcidReportDAO orcidReportDAO;
    private OrcidHelper orcidHelper;

    @Value("${captcha.site.key:}")
    private String captchaSiteKey;

    @RequestMapping(value = "/claim/all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ClaimingSummary claimAll(@RequestBody String personId, HttpServletRequest request) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, personId);

        WorkBulkResponse workBulkResponse = new WorkBulkResponse();
        Collection<SimpleEventProjection> authoredPathways = personService.getAuthoredPathways(personId);
        Collection<SimpleEventProjection> authoredReactions = personService.getAuthoredReactions(personId);
        Collection<SimpleEventProjection> reviewedPathways = personService.getReviewedPathways(personId);
        Collection<SimpleEventProjection> reviewedReactions = personService.getReviewedReactions(personId);

        // Some users are author and reviewer in certain pathways or reaction. We claim it once.
        List<SimpleEventProjection> authoredAndReviewedPathways = authoredPathways.stream().filter(reviewedPathways::contains).collect(Collectors.toList());
        List<SimpleEventProjection> authoredAndReviewedReactions = authoredReactions.stream().filter(reviewedReactions::contains).collect(Collectors.toList());
        if (!authoredAndReviewedPathways.isEmpty()) {
            authoredPathways.removeAll(authoredAndReviewedPathways);
            reviewedPathways.removeAll(authoredAndReviewedPathways);
        }
        if (!authoredAndReviewedReactions.isEmpty()) {
            authoredReactions.removeAll(authoredAndReviewedReactions);
            reviewedReactions.removeAll(authoredAndReviewedReactions);
        }

        int totalExecuted = 0;
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, authoredPathways, OrcidHelper.ContributionRole.AUTHORED, workBulkResponse);
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, authoredReactions, ContributionRole.AUTHORED, workBulkResponse);
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, reviewedPathways, ContributionRole.REVIEWED, workBulkResponse);
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, reviewedReactions, ContributionRole.REVIEWED, workBulkResponse);
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, authoredAndReviewedPathways, ContributionRole.BOTH, workBulkResponse);
        totalExecuted += orcidHelper.bulkPostWork(tokenSession, authoredAndReviewedReactions, ContributionRole.BOTH, workBulkResponse);

        orcidReportDAO.asyncPersistResponse(tokenSession, workBulkResponse);

        int total = authoredPathways.size() + reviewedPathways.size() + authoredReactions.size() + reviewedReactions.size() + authoredAndReviewedPathways.size() + authoredAndReviewedReactions.size();
        return new ClaimingSummary(total, totalExecuted, workBulkResponse);
    }

    @RequestMapping(value = "/claim/pa", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ClaimingSummary claimPathwayAuthored(@RequestBody String personId, HttpServletRequest request) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, personId);

        WorkBulkResponse workBulkResponse = new WorkBulkResponse();
        Collection<SimpleEventProjection> authoredPathways = personService.getAuthoredPathways(personId);

        int totalExecuted = orcidHelper.bulkPostWork(tokenSession, authoredPathways, ContributionRole.AUTHORED, workBulkResponse);
        orcidReportDAO.asyncPersistResponse(tokenSession, workBulkResponse);
        return new ClaimingSummary(authoredPathways.size(), totalExecuted, workBulkResponse);
    }

    @RequestMapping(value = "/claim/pr", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ClaimingSummary claimReviewedPathway(@RequestBody String personId, HttpServletRequest request) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, personId);

        WorkBulkResponse workBulkResponse = new WorkBulkResponse();
        Collection<SimpleEventProjection> reviewedPathways = personService.getReviewedPathways(personId);

        int totalExecuted = orcidHelper.bulkPostWork(tokenSession, reviewedPathways, ContributionRole.REVIEWED, workBulkResponse);
        orcidReportDAO.asyncPersistResponse(tokenSession, workBulkResponse);
        return new ClaimingSummary(reviewedPathways.size(), totalExecuted, workBulkResponse);
    }

    @RequestMapping(value = "/claim/ra", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ClaimingSummary claimAuthoredReaction(@RequestBody String personId, HttpServletRequest request) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, personId);

        WorkBulkResponse workBulkResponse = new WorkBulkResponse();
        Collection<SimpleEventProjection> authoredReactions = personService.getAuthoredReactions(personId);

        int totalExecuted = orcidHelper.bulkPostWork(tokenSession, authoredReactions, ContributionRole.AUTHORED, workBulkResponse);
        orcidReportDAO.asyncPersistResponse(tokenSession, workBulkResponse);
        return new ClaimingSummary(authoredReactions.size(), totalExecuted, workBulkResponse);
    }

    @RequestMapping(value = "/claim/rr", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ClaimingSummary claimReviewedReaction(@RequestBody String personId, HttpServletRequest request) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, personId);

        WorkBulkResponse workBulkResponse = new WorkBulkResponse();
        Collection<SimpleEventProjection> reviewedReactions = personService.getReviewedReactions(personId);
        int totalExecuted = orcidHelper.bulkPostWork(tokenSession, reviewedReactions, ContributionRole.REVIEWED, workBulkResponse);
        orcidReportDAO.asyncPersistResponse(tokenSession, workBulkResponse);
        return new ClaimingSummary(reviewedReactions.size(), totalExecuted, workBulkResponse);
    }

    @RequestMapping(value = "/{orcid:.*}/works", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Works getAllWorks(@PathVariable String orcid, HttpServletRequest request) throws IOException, WorkClaimException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        validatePerson(tokenSession, orcid);
        return orcidHelper.getAllWorks(tokenSession);
    }

    @RequestMapping(value = "/let-us-know-your-orcid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ShareYourOrcidPayload autoFillContactForm(HttpServletRequest request) throws WorkClaimException {
        OrcidToken tokenSession = orcidHelper.getAuthorisedOrcidUser(request);
        if (tokenSession == null) return null;
        final String MAIL_SUBJECT_PLACEHOLDER = "[ORCID] Please add my orcid into Reactome";
        final String MAIL_MESSAGE_PLACEHOLDER = "Dear help desk,\n\nI am logged in orcid.org and I have granted Reactome access. However my ORCID [%s] isn't in my profile. \nCould you please update my records? \n\nThank you.\n\nWe will try to get back to you as soon as possible.\n\nNOTE: This is an automatically generated message.\n\n";
        return new ShareYourOrcidPayload(
                "Let us know your orcid",
                MAIL_SUBJECT_PLACEHOLDER,
                String.format(MAIL_MESSAGE_PLACEHOLDER, tokenSession.getOrcid()),
                tokenSession,
                captchaSiteKey);
    }

    public static class ShareYourOrcidPayload {
        public final String title;
        public final String subject;
        public final String message;
        public final OrcidToken tokenSession;
        public final String captchaSiteKey;

        public ShareYourOrcidPayload(String title, String subject, String message, OrcidToken tokenSession, String captchaSiteKey) {
            this.title = title;
            this.subject = subject;
            this.message = message;
            this.tokenSession = tokenSession;
            this.captchaSiteKey = captchaSiteKey;
        }

        public String getTitle() { return title; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
        public OrcidToken getTokenSession() { return tokenSession; }
        public String getCaptchaSiteKey() { return captchaSiteKey; }
    }

    /**
     * Checking if the given person matches the authorized person in Orcid.
     * Server side security to avoid one person claiming works of another Person.
     *
     * @param tokenSession authorised user in Orcid
     * @param personId     person page in reactome
     */
    private void validatePerson(OrcidToken tokenSession, String personId) throws WorkClaimException {
        Person person = personService.findPerson(personId); //dbId or orcid
        if (person == null) throw new WorkClaimException("Person not found! RequestBody is " + personId);
        if (person.getOrcidId() == null) throw new WorkClaimException("Person doesn't have registered Orcid ID in Reactome");
        if (!person.getOrcidId().equalsIgnoreCase(tokenSession.getOrcid())) throw new WorkClaimException(String.format("Claiming not allowed. Person Orcid [%s] doesn't match authorised Orcid [%s] ", person.getOrcidId(), tokenSession.getOrcid()));
    }

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Autowired
    public void setOrcidReportDAO(OrcidReportDAO orcidReportDAO) {
        this.orcidReportDAO = orcidReportDAO;
    }

    @Autowired
    public void setOrcidHelper(OrcidHelper orcidHelper) {
        this.orcidHelper = orcidHelper;
    }
}

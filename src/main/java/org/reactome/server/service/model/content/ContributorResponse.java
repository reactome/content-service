package org.reactome.server.service.model.content;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contributor with authored/reviewed counts")
public class ContributorResponse {

    @Schema(description = "Person details")
    private SimplePerson person;

    @Schema(description = "Number of authored pathways")
    private Long authoredPathways;

    @Schema(description = "Number of reviewed pathways")
    private Long reviewedPathways;

    @Schema(description = "Number of authored reactions")
    private Long authoredReactions;

    @Schema(description = "Number of reviewed reactions")
    private Long reviewedReactions;

    public ContributorResponse() {}

    public ContributorResponse(SimplePerson person, Long authoredPathways, Long reviewedPathways,
                               Long authoredReactions, Long reviewedReactions) {
        this.person = person;
        this.authoredPathways = authoredPathways;
        this.reviewedPathways = reviewedPathways;
        this.authoredReactions = authoredReactions;
        this.reviewedReactions = reviewedReactions;
    }

    public SimplePerson getPerson() { return person; }
    public void setPerson(SimplePerson person) { this.person = person; }

    public Long getAuthoredPathways() { return authoredPathways; }
    public void setAuthoredPathways(Long authoredPathways) { this.authoredPathways = authoredPathways; }

    public Long getReviewedPathways() { return reviewedPathways; }
    public void setReviewedPathways(Long reviewedPathways) { this.reviewedPathways = reviewedPathways; }

    public Long getAuthoredReactions() { return authoredReactions; }
    public void setAuthoredReactions(Long authoredReactions) { this.authoredReactions = authoredReactions; }

    public Long getReviewedReactions() { return reviewedReactions; }
    public void setReviewedReactions(Long reviewedReactions) { this.reviewedReactions = reviewedReactions; }
}

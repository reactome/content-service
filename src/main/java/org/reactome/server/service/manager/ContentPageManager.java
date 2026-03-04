package org.reactome.server.service.manager;

import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.domain.result.DoiPathwayDTO;
import org.reactome.server.graph.domain.result.PersonAuthorReviewer;
import org.reactome.server.graph.domain.result.TocPathwayDTO;
import org.reactome.server.graph.service.DoiService;
import org.reactome.server.graph.service.PersonService;
import org.reactome.server.graph.service.TocService;
import org.reactome.server.service.model.content.*;
import org.reactome.server.service.model.content.TocPathwayResponse.TocSubpathway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContentPageManager {

    private static final Logger logger = LoggerFactory.getLogger("infoLogger");

    private TocService tocService;
    private DoiService doiService;
    private PersonService personService;

    private List<TocPathwayResponse> tocPathways = Collections.emptyList();
    private List<DoiPathwayResponse> doiPathways = Collections.emptyList();
    private List<ContributorResponse> contributors = Collections.emptyList();

    @PostConstruct
    public void init() {
        logger.info("ContentPageManager: loading TOC, DOI, and contributor data...");
        try {
            loadTocPathways();
            loadDoiPathways();
            loadContributors();
            logger.info("ContentPageManager: loaded {} TOC pathways, {} DOI pathways, {} contributors",
                    tocPathways.size(), doiPathways.size(), contributors.size());
        } catch (Exception e) {
            logger.error("ContentPageManager: error loading data", e);
        }
    }

    private void loadTocPathways() {
        Collection<TocPathwayDTO> dtos = tocService.getAllTocPathway();
        if (dtos == null) return;
        tocPathways = dtos.stream().map(this::toTocResponse).collect(Collectors.toList());
    }

    private void loadDoiPathways() {
        Collection<DoiPathwayDTO> dtos = doiService.getAllDoiPathway();
        if (dtos == null) return;
        doiPathways = dtos.stream().map(this::toDoiResponse).collect(Collectors.toList());
    }

    private void loadContributors() {
        Collection<PersonAuthorReviewer> pars = personService.getAuthorsReviewers();
        if (pars == null) return;
        contributors = pars.stream().map(this::toContributorResponse).collect(Collectors.toList());
    }

    private TocPathwayResponse toTocResponse(TocPathwayDTO dto) {
        TocPathwayResponse r = new TocPathwayResponse();
        r.setStId(dto.getStId());
        r.setDisplayName(dto.getDisplayName());
        r.setDoi(dto.getDoi());
        r.setSpecies(dto.getSpecies());
        r.setReleaseDate(dto.getReleaseDate());
        r.setReviseDate(dto.getReviseDate());
        r.setReleaseStatus(dto.getReleaseStatus());
        r.setAuthors(toSimplePersonList(dto.getAuthors()));
        r.setReviewers(toSimplePersonList(dto.getReviewers()));
        r.setEditors(toSimplePersonList(dto.getEditors()));

        if (dto.getSubpathways() != null) {
            List<TocSubpathway> subs = new ArrayList<>();
            for (Object obj : dto.getSubpathways()) {
                if (obj instanceof Event) {
                    Event ev = (Event) obj;
                    String speciesName = "";
                    List<Species> speciesList = ev.getSpecies();
                    if (speciesList != null && !speciesList.isEmpty()) {
                        speciesName = speciesList.get(0).getDisplayName();
                    }
                    subs.add(new TocSubpathway(ev.getStId(), ev.getDisplayName(), ev.getDoi(), speciesName));
                }
            }
            r.setSubpathways(subs);
        } else {
            r.setSubpathways(Collections.emptyList());
        }
        return r;
    }

    private DoiPathwayResponse toDoiResponse(DoiPathwayDTO dto) {
        DoiPathwayResponse r = new DoiPathwayResponse();
        r.setStId(dto.getStId());
        r.setDisplayName(dto.getDisplayName());
        r.setDoi(dto.getDoi());
        r.setSpecies(dto.getSpecies());
        r.setReleaseDate(dto.getReleaseDate());
        r.setReviseDate(dto.getReviseDate());
        r.setReleaseStatus(dto.getReleaseStatus());
        r.setAuthors(toSimplePersonList(dto.getAuthors()));
        r.setReviewers(toSimplePersonList(dto.getReviewers()));
        r.setEditors(toSimplePersonList(dto.getEditors()));
        return r;
    }

    private ContributorResponse toContributorResponse(PersonAuthorReviewer par) {
        SimplePerson sp = toSimplePerson(par.getPerson());
        return new ContributorResponse(
                sp,
                par.getAuthoredPathways(),
                par.getReviewedPathways(),
                par.getAuthoredReactions(),
                par.getReviewedReactions()
        );
    }

    private List<SimplePerson> toSimplePersonList(Collection<? extends Person> persons) {
        if (persons == null) return Collections.emptyList();
        return persons.stream().map(this::toSimplePerson).collect(Collectors.toList());
    }

    private SimplePerson toSimplePerson(Person p) {
        if (p == null) return null;
        return new SimplePerson(
                p.getDbId(),
                p.getDisplayName(),
                p.getSurname(),
                p.getFirstname(),
                p.getOrcidId()
        );
    }

    public List<TocPathwayResponse> getTocPathways() {
        return tocPathways;
    }

    public List<DoiPathwayResponse> getDoiPathways() {
        return doiPathways;
    }

    public List<ContributorResponse> getContributors() {
        return contributors;
    }

    @Autowired
    public void setTocService(TocService tocService) {
        this.tocService = tocService;
    }

    @Autowired
    public void setDoiService(DoiService doiService) {
        this.doiService = doiService;
    }

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}

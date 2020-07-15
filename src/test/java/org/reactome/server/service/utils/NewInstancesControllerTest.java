package org.reactome.server.service.utils;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class NewInstancesControllerTest extends BaseTest {

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    private String pathwayId;
    private String reactionId;
    private String complexId;
    private String  physicalEntityId;

    @Before
    public void getNewInstancesIds() {
        String pathwayQuery = "MATCH (p:Pathway)" +
                "Where p.releaseDate >= \"2020-03-20\"" +
                "RETURN p LIMIT 1";
        try {
            pathwayId = advancedDatabaseObjectService.getCustomQueryResult(Pathway.class, pathwayQuery).getStId();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }

        String reactionQuery = "MATCH (r:ReactionLikeEvent)" +
                "Where r.releaseDate >= \"2020-03-20\"" +
                "RETURN r LIMIT 1";
        try {
            reactionId = advancedDatabaseObjectService.getCustomQueryResult(ReactionLikeEvent.class, reactionQuery).getStId();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }

        String complexQuery = "MATCH (c:Complex) <-[:created]-(a:InstanceEdit)" +
                "WHERE a.dateTime > \"2020-03-22 16:34:04.0\"" +
                "RETURN c limit 1";
        try {
            complexId = advancedDatabaseObjectService.getCustomQueryResult(Complex.class, complexQuery).getStId();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }

        String physicalEntityIdQuery = "MATCH (pe:PhysicalEntity)<-[:created]-(a:InstanceEdit)" +
                "WHERE a.dateTime > \"2020-03-22 16:34:04.0\"" +
                "WITH pe, a " +
                "OPTIONAL MATCH (pe)<-[:referenceEntity]->(:ReferenceEntity)<-[:referenceEntity]-(k) " +
                "WITH collect(distinct k) AS arrayk, pe ,a " +
                "WHERE size(arrayk) > 0 " +
                "RETURN distinct(pe.stId) AS stId, a AS date, arrayk as otherForms limit 1";

        try {
            physicalEntityId = advancedDatabaseObjectService.getCustomQueryResult(PhysicalEntity.class, physicalEntityIdQuery).getStId();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }
    }


    //private String pathwayId = "R-MMU-5205685";
    // private String reactionId = "R-MMU-9646685";
   // private String ewasId = "R-CFA-2507846";
    //private String complexId = "R-DME-8866534";

    //private String physicalEntityId = "R-CFA-2507846";
   // physicalEntity R-CFA-2507846 has other forms
    //physicalEntity R-CFA-2187235 has no other forms


    //##################### EventController #####################//
    @Test
    public void eventPdf() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("level", 1);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");
        params.put("expColumn", 1);
        params.put("analysisProfile", "Standard");

        mockMvcGetResult("/exporter/document/event/" + pathwayId + ".pdf", "application/pdf", params);
    }


    //##################### ImageExporterControllerTest #####################//
    @Test
    public void diagramImage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        //get value from solr
        params.put("flg", "UNC5B");
        params.put("flgInteractors", true);
        params.put("title", false);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");
        //pathway
        mockMvcGetResult("/exporter/diagram/" + pathwayId + ".png", "image/png", params);
        //reaction
        mockMvcGetResult("/exporter/diagram/" + reactionId + ".png", "image/png", params);
    }

    @Test
    public void reactionImage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        params.put("flgInteractors", true);
        params.put("resource", "total");
        mockMvcGetResult("/exporter/reaction/" + reactionId + ".jpg", "image/jpg", params);
    }

    //##################### PptxExporterControllerTest #####################//
    @Test
    public void diagramPPTX() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("profile", "Modern");
        params.put("flgInteractors", true);
        //pathway
        mockMvcGetResult("/exporter/diagram/" + pathwayId + ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", params);
        //reaction
        mockMvcGetResult("/exporter/reaction/" + reactionId + ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", params);
    }

    //##################### SbxxExporterController #####################//
    @Test
    public void eventSBGN() throws Exception {
        //pathway
        //todo ERROR: Trying to write a SBGN file that already exists, but test passed
        mockMvcGetResult("/exporter/event/" + pathwayId + " .sbgn", "application/sbgn+xml");
        //reaction
        mockMvcGetResult("/exporter/event/" + reactionId + ".sbgn", "application/sbgn+xml");
    }

    @Test
    public void eventSBML() throws Exception {
        //pathway
        mockMvcGetResult("/exporter/event/" + pathwayId + ".sbml", "application/sbml+xml");
        //reaction
        mockMvcGetResult("/exporter/event/" + reactionId + ".sbml", "application/sbml+xml");
        //pathway in V73
        mockMvcGetResult("/exporter/event/R-HSA-9679191.sbml", "application/sbml+xml");
    }

    //##################### DiscoverControllerTest #####################//
    @Test
    public void eventDiscovery() throws Exception {
        //pathway
        mockMvcGetResult("/data/discover/" + pathwayId, "application/json;charset=UTF-8");
        //reaction
        mockMvcGetResult("/data/discover/" + reactionId, "application/json;charset=UTF-8");
    }

    //##################### EventControllerTest #####################//
    @Test
    public void getEventAncestors() throws Exception {
        //pathway
        mockMvcGetResult("/data/event/" + pathwayId + "/ancestors", "application/json;Charset=UTF-8");
        //reaction
        mockMvcGetResult("/data/event/" + reactionId + "/ancestors", "application/json;Charset=UTF-8");
    }

    //##################### ParticipantsControllerTest #####################//
    @Test
    public void getParticipants() throws Exception {
        mockMvcGetResult("/data/participants/" + pathwayId, "application/json;Charset=UTF-8");
    }

    @Test
    public void getParticipatingPhysicalEntities() throws Exception {
        mockMvcGetResult("/data/participants/" + pathwayId + "/participatingPhysicalEntities", "application/json;Charset=UTF-8");
    }

    @Test
    public void getParticipatingReferenceEntities() throws Exception {
        mockMvcGetResult("/data/participants/" + pathwayId + "referenceEntities", "application/json;Charset=UTF-8");
    }

    //##################### PathwayControllerTest #####################//
    @Test
    public void getContainedEvents() throws Exception {
        mockMvcGetResult("/data/pathway/" + pathwayId + "/containedEvents", "application/json;Charset=UTF-8");
    }

    @Test
    public void getContainedEventsWithAttribute() throws Exception {
        mockMvcGetResult("/data/pathway/" + pathwayId + "/containedEvents/displayName", "text/plain;charset=ISO-8859-1");
    }

    //##################### PhysicalEntityControllerTest #####################//
    @Test
    public void getOtherFormsOf() throws Exception {
        mockMvcGetResult("/data/entity/" + physicalEntityId + "/otherForms", "application/json;Charset=UTF-8");
    }

    @Test
    public void getComponentsOf() throws Exception {
        mockMvcGetResult("/data/entity/" + pathwayId + "/componentOf", "application/json;Charset=UTF-8");
    }

    //todo needs a new complex id
    @Test
    public void getComplexSubunits() throws Exception {
        mockMvcGetResult("/data/complex/" + complexId + "/subunits", "application/json;Charset=UTF-8", "excludeStructures", "false");
    }

    //##################### QueryObjectControllerTest #####################//
    @Test
    public void findById() throws Exception {
        mockMvcGetResult("/data/query/" + pathwayId, "application/json;Charset=UTF-8");
    }

    @Test
    public void findByIdAttributeName() throws Exception {
        mockMvcGetResult("/data/query/" + "R-COV-9685513" + "/hasModifiedResidue", "text/plain;charset=ISO-8859-1");
    }

    @Test
    public void findEnhancedObjectById() throws Exception {
        mockMvcGetResult("/data/query/enhanced/" + pathwayId, "application/json;charset=UTF-8");
    }

    //##################### SearchControllerTest #####################//
    //needs species and query item like MAD

}

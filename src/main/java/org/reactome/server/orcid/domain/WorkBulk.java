package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class WorkBulk implements Serializable {

    @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
    private List<Work> bulk;

    public static void main(String[] args) throws Exception {
//        WorkBulk b = new WorkBulk();
//        b.setBulk(new ArrayList<>());
//
//        for (int i = 30; i <= 40; i++) {
//            Work work = new Work();
//            work.setJournalTitle(new Value("My Journal Title " + i));
//            work.setType("DATA_SET");
//            work.setUrl(new Value("https://reactome.org/content/detail/R-HSA-199420"));
//            work.setWorkTitle(new WorkTitle(new Value("My Title " + i)));
//            ExternalIds externalIds = new ExternalIds();
//            externalIds.setExternalId(new ArrayList<>());
//            externalIds.getExternalId().add(new ExternalId("doi", "10.111" + Math.random(), "https://doi.org/10.111" + i, "SELF"));
//            externalIds.getExternalId().add(new ExternalId("doi", "10.112" + Math.random(), "https://doi.org/10.112" + i, "SELF"));
//            work.setExternalIds(externalIds);
//
//            b.getBulk().add(work);
//        }
//
//        ObjectMapper aa = new ObjectMapper();
//        aa.enable(SerializationFeature.INDENT_OUTPUT);
//        System.out.println(aa.writeValueAsString(b));

        File f = new File("/Users/gsviteri/gui.json");
        ObjectMapper ss = new ObjectMapper();
        ss.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        WorkBulk s = ss.readValue(f, WorkBulk.class);
        System.out.println(s);

    }

    public List<Work> getBulk() {
        return bulk;
    }

    public void setBulk(List<Work> bulk) {
        this.bulk = bulk;
    }

    @Override
    public String toString() {
        return "WorkBulk{" +
                "bulk=" + bulk +
                '}';
    }
}

package org.reactome.server.service.controller.interactors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class CustomInteractorsControllerTest extends BaseTest {

    @Test
    public void postFile() throws Exception {

        //PSI-MI Tab
        String pSIMITABF = "uniprotkb:Q0PC27\tuniprotkb:Q0P8D3\t-\t-\tuniprotkb:EXBD3(gene name)\tuniprotkb:Q0P8D3(gene name)\tpsi-mi:\"MI:0397\"(two hybrid array)\t-\tpubmed:17615063\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0469\"(IntAct)\tintact:EBI-1345937\tmentha-score:0.126\n" +
                "uniprotkb:P78406\tuniprotkb:Q9NVU0\t-\t-\tuniprotkb:RAE1(gene name)\tuniprotkb:POLR3E(gene name)\tpsi-mi:\"MI:0004\"(affinity chromatography technology)\t-\tpubmed:19615732\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:424131\tmentha-score:0.126\n" +
                "uniprotkb:Q58EK5\tuniprotkb:Q6DGV5\t-\t-\tuniprotkb:TDRD1(gene name)\tuniprotkb:RAB10(gene name)\tpsi-mi:\"MI:0006\"(anti bait coimmunoprecipitation)\t-\tpubmed:21743441\ttaxid:7955(Danio rerio)\ttaxid:7955(Danio rerio)\tpsi-mi:\"MI:0914\"(association)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-8283801\tmentha-score:0.126\n" +
                "uniprotkb:P32801\tuniprotkb:P40084\t-\t-\tuniprotkb:ELM1(gene name)\tuniprotkb:RTR1(gene name)\tpsi-mi:\"MI:0096\"(pull down)\t-\tpubmed:21460040\ttaxid:559292(Saccharomyces cerevisiae S288c)\ttaxid:559292(Saccharomyces cerevisiae S288c)\tpsi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:520437\tmentha-score:0.183\n" +
                "uniprotkb:Q0PC27\tuniprotkb:Q0P8F5\t-\t-\tuniprotkb:EXBD3(gene name)\tuniprotkb:Q0P8F5(gene name)\tpsi-mi:\"MI:0397\"(two hybrid array)\t-\tpubmed:17615063\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0469\"(IntAct)\tintact:EBI-1361363\tmentha-score:0.126\n" +
                "uniprotkb:P78406\tuniprotkb:Q9NW08\t-\t-\tuniprotkb:RAE1(gene name)\tuniprotkb:POLR3B(gene name)\tpsi-mi:\"MI:0004\"(affinity chromatography technology)\t-\tpubmed:19615732\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:424129\tmentha-score:0.126\n" +
                "uniprotkb:Q58EK5\tuniprotkb:Q6GQM1\t-\t-\tuniprotkb:TDRD1(gene name)\tuniprotkb:TUBA7L(gene name)\tpsi-mi:\"MI:0006\"(anti bait coimmunoprecipitation)\t-\tpubmed:21743441\ttaxid:7955(Danio rerio)\ttaxid:7955(Danio rerio)\tpsi-mi:\"MI:0914\"(association)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-8283801\tmentha-score:0.126\n" +
                "uniprotkb:P32801\tuniprotkb:P43549\t-\t-\tuniprotkb:ELM1(gene name)\tuniprotkb:P43549(gene name)\tpsi-mi:\"MI:0363\"(inferred by author)\t-\tpubmed:16554755\ttaxid:559292(Saccharomyces cerevisiae S288c)\ttaxid:559292(Saccharomyces cerevisiae S288c)\tpsi-mi:\"MI:0914\"(association)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-2780648\tmentha-score:0.126\n" +
                "uniprotkb:Q0PC27\tuniprotkb:Q0P8F9\t-\t-\tuniprotkb:EXBD3(gene name)\tuniprotkb:Q0P8F9(gene name)\tpsi-mi:\"MI:0397\"(two hybrid array)\t-\tpubmed:17615063\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0469\"(IntAct)\tintact:EBI-1345004\tmentha-score:0.126\n" +
                "uniprotkb:P78406\tuniprotkb:Q9NXC5\t-\t-\tuniprotkb:RAE1(gene name)\tuniprotkb:MIOS(gene name)\tpsi-mi:\"MI:0004\"(affinity chromatography technology)\t-\tpubmed:19615732\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:424069\tmentha-score:0.126\n" +
                "uniprotkb:Q58EK5\tuniprotkb:Q6IMF9\t-\t-\tuniprotkb:TDRD1(gene name)\tuniprotkb:TFA(gene name)\tpsi-mi:\"MI:0006\"(anti bait coimmunoprecipitation)\t-\tpubmed:21743441\ttaxid:7955(Danio rerio)\ttaxid:7955(Danio rerio)\tpsi-mi:\"MI:0914\"(association)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-8283801\tmentha-score:0.126\n" +
                "uniprotkb:P32801\tuniprotkb:P47113\t-\t-\tuniprotkb:ELM1(gene name)\tuniprotkb:BFA1(gene name)\tpsi-mi:\"MI:0415\"(enzymatic study)\t-\tpubmed:20855503\ttaxid:559292(Saccharomyces cerevisiae S288c)\ttaxid:559292(Saccharomyces cerevisiae S288c)\tpsi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:614242\tmentha-score:0.309\n" +
                "uniprotkb:Q0PC27\tuniprotkb:Q0P8I8\t-\t-\tuniprotkb:EXBD3(gene name)\tuniprotkb:Q0P8I8(gene name)\tpsi-mi:\"MI:0397\"(two hybrid array)\t-\tpubmed:17615063\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\ttaxid:192222(Campylobacter jejuni subsp. jejuni NCTC 11168 = ATCC 700819)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0469\"(IntAct)\tintact:EBI-1356884\tmentha-score:0.126\n" +
                "uniprotkb:Q9UBU9\tuniprotkb:P78406\t-\t-\tuniprotkb:NXF1(gene name)\tuniprotkb:RAE1(gene name)\tpsi-mi:\"MI:0096\"(pull down)\t-\tpubmed:10668806\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-14383\tmentha-score:0.569\n" +
                "uniprotkb:Q9UBU9\tuniprotkb:P78406\t-\t-\tuniprotkb:NXF1(gene name)\tuniprotkb:RAE1(gene name)\tpsi-mi:\"MI:0004\"(affinity chromatography technology)\t-\tpubmed:10668806\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:718103\tmentha-score:0.569\n" +
                "uniprotkb:P78406\tuniprotkb:Q9UBU9\t-\t-\tuniprotkb:RAE1(gene name)\tuniprotkb:NXF1(gene name)\tpsi-mi:\"MI:0019\"(coimmunoprecipitation)\t-\tpubmed:10668806\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0471\"(MINT)\tmint:MINT-14382\tmentha-score:0.569";


        String extendFile = "#ID A\tID B\tALIAS A\tALIAS B\tTAX_ID A\tTAX_ID B\tEVIDENCE\t\tSCORE\n" +
                "Q13501\tQ9H0R8\tSQSTM\tGBRL1\t9606\t\t9606\t\tEBI-1010739\t\t0.48\n" +
                "Q9GZQ8\tP41743\tMLP3B\tKPCI\t9606\t\t9606\t\tEBI-2604042\t\t0.79\n" +
                "Q14596\tQ13501\tNBR1\tSQSTM\t9606\t\t9606\t\tEBI-2947446\t\t0.22\n" +
                "Q14596\tQ13501\tNBR1\tSQSTM\t9606\t\t9606\t\tEBI-755491\t\t0.96\n" +
                "Q13501\tQ9GZQ8\tSQSTM\tMLP3B\t9606\t\t9606\t\tEBI-2603774\t\t0.85\n" +
                "P41743\tQ13501\tKPCI\tGBRL1\t9606\t\t9606\t\tEBI-3197899\t\t0.74\n" +
                "Q9H0R8\tQ14596\tMLP3B\tNBR1\t9606\t\t9606\t\tEBI-8276688\t\t0.49\n" +
                "Q9H0R8\tQ14596\tMLP3B\tNBR1\t9606\t\t9606\t\tEBI-7392199\t\t0.67";

        String tsvFile = "#ID A\tID B    \n" +
                "Q9UBU9\tP78406\n" +
                "P84103\tQ9UBU9\n" +
                "Q9BY44\tQ9UBU9";

        MockMultipartFile PSIMITabFileTest = new MockMultipartFile("file", "tuple-mentha-psimitab-ex.txt", "multipart/form-data", pSIMITABF.getBytes());
        MockMultipartFile extendFileTest = new MockMultipartFile("file", "extend-ex.txt", "multipart/form-data", extendFile.getBytes());
        MockMultipartFile tsvFileTest = new MockMultipartFile("file", "tsv-ex.txt", "multipart/form-data", tsvFile.getBytes());


        List<MockMultipartFile> files = new ArrayList<>();
        files.add(PSIMITabFileTest);
        files.add(extendFileTest);
        files.add(tsvFileTest);

        MockMultipartHttpServletRequestBuilder requestBuilder = fileUpload("/interactors/upload/tuple/form");

        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }

        this.getMockMvc().perform(requestBuilder
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "fileUploadTest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
    }

    @Test
    public void postFileContent() throws Exception {

        String content = "uniprotkb:Q9UBU9\tuniprotkb:P78406\t-\t-\tuniprotkb:NXF1(gene name)\tuniprotkb:RAE1(gene name)\tpsi-mi:\"MI:0004\"(affinity chromatography technology)\t-\tpubmed:10668806\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)\tpsi-mi:\"MI:0915\"(physical association)\tpsi-mi:\"MI:0463\"(biogrid)\tbiogrid:718103\tmentha-score:0.569";

        mvcPostResult("/interactors/upload/tuple/content", content, "name", "CSTest");

    }

    @Test
    public void postUrl() throws Exception {

        String url = "http://mentha.uniroma2.it:9090/psicquic/webservices/current/search/query/Q9UBU9";

        mvcPostResult("/interactors/upload/tuple/url", url, "name", "CSTest");
    }
}
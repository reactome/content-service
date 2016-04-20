package org.reactome.server.service.manager;

import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.reactome.server.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.interactors.model.InteractionDetails;
import org.reactome.server.interactors.service.InteractionService;
import org.reactome.server.interactors.service.PsicquicService;
import org.reactome.server.interactors.util.InteractorConstant;
import org.reactome.server.service.exception.InteractorResourceNotFound;
import org.reactome.server.service.exception.PsicquicContentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import psidev.psi.mi.tab.PsimiTabException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Component
public class DownloadManager {

    /**
     * Holds the services that query the DB
     **/
    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PsicquicService psicquicService;

    /**
     * Download IntAct (static) molecule interactions for a given accession
     *
     * @param acc      the accession
     * @param filename is the result filename, user can change it.
     * @return FileSystemResource
     * @throws IOException
     */
    public FileSystemResource downloadStaticMoleculeInteractions(String acc, String filename) throws IOException {
        return downloadStaticMoleculesInteractions(Collections.singletonList(acc), filename);
    }

    /**
     * Download IntAct (static) molecule interactions for a given accessions
     *
     * @param accs     the accession list
     * @param filename is the result filename, user can change it.
     * @return FileSystemResource
     * @throws IOException
     */
    public FileSystemResource downloadStaticMoleculesInteractions(Collection<String> accs, String filename) throws IOException {
        try {
            Map<String, List<Interaction>> interactionsMap = interactionService.getInteractions(accs, InteractorConstant.STATIC);
            File downloadFile = prepareDownloadFile(interactionsMap, filename);
            return new FileSystemResource(downloadFile);

        } catch (SQLException | InvalidInteractionResourceException s) {
            s.printStackTrace();
            throw new InteractorResourceNotFound(InteractorConstant.STATIC);
        }
    }

    /**
     * Download PSICQUIC molecule interactions for a given accession
     *
     * @param acc      the accession
     * @param resource PSICQUIC resource
     * @param filename is the result filename, user can change it.
     * @return FileSystemResource
     * @throws IOException
     */
    public FileSystemResource downloadPsicquicMoleculeInteractions(String acc, String resource, String filename) throws IOException {
        return downloadPsicquicMoleculesInteractions(Collections.singletonList(acc), resource, filename);
    }

    /**
     * Download PSICQUIC molecule interactions for a given accessions
     *
     * @param accs     the accession list
     * @param resource PSICQUIC resource
     * @param filename is the result filename, user can change it.
     * @return FileSystemResource
     * @throws IOException
     */
    public FileSystemResource downloadPsicquicMoleculesInteractions(Collection<String> accs, String resource, String filename) throws IOException {
        try {
            Map<String, List<Interaction>> interactionsMap = psicquicService.getInteractions(resource, accs);
            File downloadFile = prepareDownloadFile(interactionsMap, filename);
            return new FileSystemResource(downloadFile);

        } catch (PsicquicQueryException e) {
            throw new PsicquicContentException("Resource is not responding.");
        } catch (PsimiTabException e) {
            throw new PsicquicContentException("Couldn't parse psimitab file.");
        } catch (PsicquicRegistryClientException e) {
            throw new PsicquicContentException("Couldn't load Resource");
        }
    }

    /**
     * Gets the interactions and writes in the result file.
     *
     * @param interactionsMap is the whole results, having the accession as the Key and its interactions as the value
     * @param filename        is the result filename, user can change it.
     * @return the result file
     * @throws IOException
     */
    private File prepareDownloadFile(Map<String, List<Interaction>> interactionsMap, String filename) throws IOException {
        final String HEADER = "Interactor A\tInteractor B\tmiScore\tEvidences\n";

        StringBuilder builder = new StringBuilder();

        File file = File.createTempFile(filename, "csv");
        FileWriter fw = new FileWriter(file);

        fw.write(HEADER);

        for (String accessionKey : interactionsMap.keySet()) {
            List<Interaction> interactions = interactionsMap.get(accessionKey);

            for (Interaction interaction : interactions) {

                builder.append(interaction.getInteractorA().getAcc()).append("\t");
                builder.append(interaction.getInteractorB().getAcc()).append("\t");
                builder.append(interaction.getIntactScore()).append("\t");

                String evidences = "";
                String delim = "";
                for (InteractionDetails evidence : interaction.getInteractionDetailsList()) {
                    evidences = evidences.concat(delim);
                    delim = ";";
                    evidences = evidences + evidence.getInteractionAc();
                }

                builder.append(evidences).append("\t\n");
            }

            // write accession
            fw.write(builder.toString());
            fw.flush();

            // renew buffer
            builder = new StringBuilder();

        }

        fw.flush();
        fw.close();

        return file;
    }
}

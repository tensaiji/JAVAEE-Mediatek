package services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mediatek2022.Document;
import mediatek2022.Utilisateur;
import services.api.APIDoc;
import services.base.Service;
import services.base.ServiceAbonne;
import services.base.ServiceAuthentification;

/**
 * Service d'affichage d'un document.
 */
public class PageDocument extends ServiceAbonne
{
    /**
     * Construction du service.
     */
    public PageDocument()
    {
        super("document", true);
    }

    /** Paramètre définissant l'identifiant numérique du document à afficher. */
    private static final String PARAM_ID = "id";

    /** Nom et identifiant du boutton associé à l'action EMPRUNTER. */
    private static final String ACTION_EMPRUNTER = "emprunter";

    @Override
    protected void pre(HttpServletRequest requete, HttpServletResponse reponse) 
    {
        requete.setAttribute("ACTION_EMPRUNTER", ACTION_EMPRUNTER);
    }

    /**
     * Récupération de l'identifiant numérique du document depuis l'URL (GET).
     * Une redirection est effectuée si le paramètre n'est pas défini.
     * @param requete Requête HTTP.
     * @param reponse Réponse HTTP.
     * @return Document récupéré, null si aucun document n'a pu être récupéré.
     */
    private final Document recuperer_document(HttpServletRequest requete, HttpServletResponse reponse)
    {
        final String id_s = requete.getParameter(PARAM_ID);
        if (id_s != null)
        {
            try
            {
                final int id = Integer.parseInt(id_s);
                synchronized(MEDIATHEQUE) { return MEDIATHEQUE.getDocument(id); }
            }
            catch (NumberFormatException e) { e.printStackTrace(); }
        }
        else Service.redirection("", false, requete, reponse);
        return null;
    }

    @Override
    protected void pre_page(HttpServletRequest requete, HttpServletResponse reponse) 
    {
        // Récupération dans la médiathèque depuis l'identifiant.
        final Document doc = this.recuperer_document(requete, reponse);
        if (doc != null) // Enregistrement des métadonnées en tant qu'attribut de requête.
            requete.setAttribute(PARAM_DOCUMENT, APIDoc.meta(doc));
        else
        {
            try { reponse.sendError(404, "Document introuvable."); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void POST(HttpServletRequest requete, HttpServletResponse reponse)
    {
        final String emprunter = requete.getParameter(ACTION_EMPRUNTER);
        if (emprunter != null)
        {
            final Document doc = this.recuperer_document(requete, reponse);
            if (doc != null)
            {
                try 
                { 
                    synchronized(MEDIATHEQUE) 
                    { MEDIATHEQUE.emprunt(doc, (Utilisateur)requete.getSession().getAttribute(ServiceAuthentification.PARAM_UTILISATEUR)); }
                    requete.setAttribute(PARAM_MSG, "Profitez bien de votre emprunt !");
                }
                catch (Exception e) { requete.setAttribute(PARAM_MSG, e.getMessage()); }
            }
        }
    }
}

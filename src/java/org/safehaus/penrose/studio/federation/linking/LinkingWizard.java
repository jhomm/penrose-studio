package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;

import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class LinkingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private DN dn;
    private SearchResult searchResult;
    private Source source;
    private DN baseDn;
    private Collection<SearchResult> results;

    LinkingSearchPage searchPage;
    LinkingResultsPage resultsPage;

    public LinkingWizard() {
        setWindowTitle("Linking Wizard");
    }

    public void addPages() {
        searchPage = new LinkingSearchPage();
        searchPage.setDn(dn);
        searchPage.setSearchResult(searchResult);
        searchPage.setSource(source);
        addPage(searchPage);

        resultsPage = new LinkingResultsPage();
        resultsPage.setDn(dn);
        resultsPage.setEntry(searchResult);
        resultsPage.setSource(source);
        resultsPage.setBaseDn(baseDn);
        addPage(resultsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (page == searchPage) {

            DN searchBaseDn = new DN(searchPage.getBaseDn()).getPrefix(baseDn);
            String filter = searchPage.getFilter();
            int scope = searchPage.getScope();

            Collection<SearchResult> results = search(searchBaseDn, filter, scope);

            resultsPage.setResults(results);

            return resultsPage;

        } else {
            return super.getNextPage(page);
        }
    }

    public Collection<SearchResult> search(DN baseDn, String filter, int scope) {
        try {

            SearchRequest request = new SearchRequest();
            request.setDn(baseDn);
            request.setFilter(filter);
            request.setScope(scope);

            SearchResponse response = new SearchResponse();

            source.search(request, response);

            return response.getAll();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public boolean performFinish() {
        try {
            results = resultsPage.getSelections();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public DN getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(DN baseDn) {
        this.baseDn = baseDn;
    }

    public Collection<SearchResult> getResults() {
        return results;
    }

    public void setResults(Collection<SearchResult> results) {
        this.results = results;
    }
}

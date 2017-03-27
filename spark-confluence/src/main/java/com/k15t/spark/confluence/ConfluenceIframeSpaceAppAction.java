package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;


/**
 * Class that can be extended for creating an action that opens a SPA in an iframe in the Space Tools view.
 */
public abstract class ConfluenceIframeSpaceAppAction extends AbstractSpaceAction implements SpaceAware {

    private String body;


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    public String index() {

        try {
            String appBaseUrl = ServletActionContext.getRequest().getContextPath() + "/" +
                    StringUtils.removeStart(getSpaBaseUrl(), "/");
            long idSuffix = System.currentTimeMillis();

            String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();
            Map<String, Object> context = DocumentOutputUtil.generateAdminIframeTemplateContext(
                    appBaseUrl, "spark_space_adm_iframe_" + idSuffix,
                    getIframeContextInfo(), getSpaQueryString());

            this.body = VelocityUtils.getRenderedContent(template, context);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load iframe-space app template");
        }

        return INPUT;
    }


    /**
     * <p>
     * The result of this method will be available in the context of the loaded iframe using SPARK.getContextData()
     * </p><p>
     * The JS variable will be a string. To pass structured information eg. JSON can be used.
     * </p><p>
     * This is the main method of injecting context specific information to a space app iframe. The passed
     * information can be customized to fit the needed use case by sub-classing this class with an
     * action class that will override this method (and by using that class in the correct atlassian module
     * specification).
     * </p>
     *
     * @return string that will be attached to SPARK.iframeContext variable as a JS string
     */
    protected String getIframeContextInfo() {
        return "{\"space_key\": \"" + getSpaceKey() + "\"}";
    }


    /**
     * <p>
     * The query parameter to add as the current parameter to the url of the SPA app when it is loaded into
     * the iframe context ("iframe_content=true" will always be added and used by the SPARK framework)
     * </p><p>
     * Default implementation returns the query string used when running the action
     * </p>
     *
     * @return query string to use when loading the SPA in the iframe
     */
    protected String getSpaQueryString() {
        return ServletActionContext.getRequest().getQueryString();
    }


    /**
     * Returns the base url of the single page application relative to the Confluence context path. Can be overwritten by subclasses.
     * This default implementation evaluates the action parameter with name {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}.
     *
     * @return the base url of the spa app
     */
    protected String getSpaBaseUrl() {
        ActionConfig actionConfig = ServletActionContext.getContext().getActionInvocation().getProxy().getConfig();
        Object o = actionConfig.getParams().get(Keys.SPARK_SPA_BASE_URL);
        String baseUrl = (o instanceof String) ? (String) o : null;
        if (StringUtils.isNotBlank(baseUrl)) {
            return baseUrl;
        } else {
            throw new IllegalStateException("Missing action parameter '" + Keys.SPARK_SPA_BASE_URL + "'. Either add the parameter to "
                    + "the action definition in atlassian-plugin.xml or overwrite 'getSpaBaseUrl()' in your action implementation.");
        }
    }


    /**
     * @return string to be used as the title of the iframe wrapper page
     */
    public abstract String getTitleAsHtml();


    /**
     * Returns the complete module key of the space tools web-item to be marked as selected. Can be overwritten by subclasses.
     * This default implementation first checks for a request parameter and then for an action parameter with name
     * {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}. If none are present {@code null} is returned.
     */
    public String getSelectedSpaceToolsWebItem() {
        String selectedWebItemKey = ServletActionContext.getRequest().getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY);
        if (selectedWebItemKey == null) {
            ActionConfig actionConfig = ServletActionContext.getContext().getActionInvocation().getProxy().getConfig();
            Object o = actionConfig.getParams().get(Keys.SPARK_SELECTED_WEB_ITEM_KEY);
            selectedWebItemKey = (o instanceof String) ? (String) o : null;
        }
        return selectedWebItemKey;
    }


    @Override
    public boolean isSpaceRequired() {
        return true;
    }


    @Override
    public boolean isViewPermissionRequired() {
        return true;
    }


    /**
     * @return main body html of the iframe wrapper
     */
    public String getBodyAsHtml() {
        return body;
    }

}

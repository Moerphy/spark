package com.k15t.spark.confluence;

import com.opensymphony.xwork.Action;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;

import static org.mockito.Mockito.times;


public class ConfluenceIframeAdminAppActionTest extends ConfluenceSpaceAppActionTestCommon {

    private ConfluenceIframeAdminAppAction actionInstance;

    public static class ConfluenceIframeAdminAppActionBaseTestImpl extends ConfluenceIframeAdminAppAction {

        @Override
        public String getSpaBaseUrl() {
            return "/spark/admin/testapp/baseurl/";
        }


        @Override
        public String getTitleAsHtml() {
            return null;
        }


    }


    @Before
    public void setup() throws Exception {
        super.commonSetup();

        actionInstance = new ConfluenceIframeAdminAppActionBaseTestImpl();

    }


    @Test
    public void runActionWithIframeAppResourcePath() throws IOException {

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value
        // mostly checks here that the work is delegated to helper with corrent arguments and correct res used

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // expect the actual spark iframe body rendering to be delegated to helper class
        PowerMockito.verifyStatic(times(1));
        ConfluenceIframeSparkActionHelper.renderSparkIframeBody(
                actionInstance, servletRequest, "spark_admin_iframe_");

        Assert.assertEquals(renderedVelocityToReturn, actionInstance.getBodyAsHtml());

    }


    @Test
    public void iframeAdminActionDefaultQueryParamHandling() throws IOException {

        Mockito.when(servletRequest.getQueryString()).thenReturn("?entry_point=admin");
        Assert.assertEquals("?entry_point=admin", actionInstance.getSpaQueryString());

        Mockito.when(servletRequest.getQueryString()).thenReturn("");
        Assert.assertEquals("", actionInstance.getSpaQueryString());

        Mockito.when(servletRequest.getQueryString()).thenReturn("test_param=value&other=42&third=75");
        Assert.assertEquals("test_param=value&other=42&third=75", actionInstance.getSpaQueryString());

    }

}
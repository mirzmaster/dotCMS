package com.dotmarketing.portlets.structure.business;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;

import com.dotcms.TestBase;
import com.dotcms.publisher.business.PublisherAPIImpl;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.MultiTree;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.db.HibernateUtil;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.factories.MultiTreeFactory;
import com.dotmarketing.factories.WebAssetFactory;
import com.dotmarketing.portlets.containers.model.Container;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.templates.model.Template;
import com.dotmarketing.servlets.test.ServletTestRunner;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

public class URLMapTest extends TestBase  {

	@Before
	public void init() {

	}


	@Test
	public void testURLMaps() throws Exception {

		Folder testFolder = null;
		Template template = null;
		Container container = null;
		Structure testSt = null;
		Contentlet widget = null;
		User user = null;


		try {

			user = APILocator.getUserAPI().getSystemUser();
			Host demoHost = APILocator.getHostAPI().findByName("demo.dotcms.com", user, false);

			HibernateUtil.startTransaction();

			// CONTAINER
			container = new Container();

			container.setCode( "$!{story}" );
			container.setFriendlyName( "newsTestContainer" );
			container.setIDate(new Date());
			container.setLuceneQuery("");
			container.setMaxContentlets(1);
			container.setModDate(new Date());
			container.setModUser(user.getUserId());
			container.setNotes("newsTestContainer");
			container.setOwner(user.getUserId());
			container.setPostLoop("");
			container.setPreLoop("");
			container.setShowOnMenu(true);
			container.setSortContentletsBy("");
			container.setSortOrder(2);
			container.setStaticify(true);
			container.setTitle("News Test Container");
			container.setType("containers");
			container.setUseDiv( true );

			WebAssetFactory.createAsset( container, user.getUserId(), demoHost );
			APILocator.getVersionableAPI().setLive( container );


			// TEMPLATE
			template = new Template();

			String body = "#parseContainer('" + container.getIdentifier() + "')";
			template.setBody( body );
			template.setFooter( "" );
			template.setFriendlyName( "newsTestTemplate" );
			template.setHeader( "" );
			template.setIDate( new Date() );
			template.setImage( "" );
			template.setModDate( new Date() );
			template.setModUser( user.getUserId() );
			template.setOwner( user.getUserId() );
			template.setSelectedimage( "" );
			template.setShowOnMenu( true );
			template.setSortOrder( 2 );
			template.setTitle( "News Test Template" );
			template.setType( "template" );

			template = APILocator.getTemplateAPI().saveTemplate( template, demoHost, user, false );
			APILocator.getVersionableAPI().setLive(template);


			// FOLDER
			testFolder = new Folder();

			testFolder.setFilesMasks( "" );
			testFolder.setIDate( new Date() );
			testFolder.setName( "news_test_folder_" + String.valueOf( new Date().getTime() ) );
			testFolder.setOwner( user.getUserId() );
			testFolder.setShowOnMenu( false );
			testFolder.setSortOrder( 0 );
			testFolder.setTitle( "news_test_folder_" + String.valueOf( new Date().getTime() ) );
			testFolder.setType( "folder" );
			testFolder.setHostId( demoHost.getIdentifier() );
			//Creates and set an identifier
			Identifier identifier = APILocator.getIdentifierAPI().createNew( testFolder, demoHost );
			testFolder.setIdentifier( identifier.getId() );

			APILocator.getFolderAPI().save( testFolder, user, false );


			// HTMLPAGE
			HTMLPage htmlPage = new HTMLPage();

			htmlPage.setEndDate( new Date() );
			htmlPage.setFriendlyName( "newstest-detail.html" );
			htmlPage.setIDate( new Date() );
			htmlPage.setMetadata( "" );
			htmlPage.setModDate( new Date() );
			htmlPage.setModUser( user.getUserId() );
			htmlPage.setOwner( user.getUserId() );
			htmlPage.setPageUrl( "newstest-detail.html" );
			htmlPage.setRedirect( "" );
			htmlPage.setShowOnMenu( true );
			htmlPage.setSortOrder( 2 );
			htmlPage.setStartDate( new Date() );
			htmlPage.setTitle( "News Test Detail" );
			htmlPage.setType( "htmlpage" );
			htmlPage.setWebEndDate( "" );
			htmlPage.setWebStartDate( "" );

			htmlPage = APILocator.getHTMLPageAPI().saveHTMLPage( htmlPage, template, testFolder, user, false );

			PermissionAPI permissionAPI = APILocator.getPermissionAPI();
			RoleAPI roleAPI = APILocator.getRoleAPI();

			Permission newPermission = new Permission( htmlPage.getPermissionId(), roleAPI.loadCMSAnonymousRole().getId(), PermissionAPI.PERMISSION_READ, true );
			permissionAPI.save( newPermission, htmlPage, user, false );

			//Make it working and live
			APILocator.getVersionableAPI().setWorking( htmlPage );
			APILocator.getVersionableAPI().setLive( htmlPage );

			// WIDGET
			Structure simpleWidgetSt = StructureCache.getStructureByVelocityVarName("SimpleWidget");

			widget = new Contentlet();
			widget.setReviewInterval("1m");
			widget.setStructureInode(simpleWidgetSt.getInode());
			widget.setHost(demoHost.getIdentifier());

			Field widgetTitle =  simpleWidgetSt.getFieldVar("widgetTitle");
			Field widgetCode = simpleWidgetSt.getFieldVar("code");


			ContentletAPI contentletAPI = APILocator.getContentletAPI();

			contentletAPI.setContentletProperty( widget, widgetTitle, "newsTestWidget" );
			contentletAPI.setContentletProperty( widget, widgetCode, "$URLMapContent.story" );

			widget = contentletAPI.checkin( widget, null, permissionAPI.getPermissions( simpleWidgetSt ), user, false );
			APILocator.getVersionableAPI().setLive(widget);

			// add the widget to the detail page
			MultiTreeFactory.saveMultiTree( new MultiTree( htmlPage.getIdentifier(), container.getIdentifier(), widget.getIdentifier() ) );


			// STRUCTURE
			testSt = new Structure();

			testSt.setDefaultStructure( false );
			testSt.setDescription( "News Test" );
			testSt.setFixed( false );
			testSt.setIDate( new Date() );
			testSt.setName( "NewsTest" );
			testSt.setOwner( user.getUserId() );
			testSt.setStructureType( Structure.STRUCTURE_TYPE_CONTENT );
			testSt.setType( "structure" );
			testSt.setVelocityVarName( "NewsTest" );
			testSt.setUrlMapPattern("/newstest/{urlNewsTitle}");
			testSt.setDetailPage( htmlPage.getIdentifier() );

			StructureFactory.saveStructure( testSt );

			//Creating and adding permissions
			Permission permissionRead = new Permission( testSt.getInode(), roleAPI.loadCMSAnonymousRole().getId(), PermissionAPI.PERMISSION_READ );
			Permission permissionEdit = new Permission( testSt.getInode(), roleAPI.loadCMSAnonymousRole().getId(), PermissionAPI.PERMISSION_EDIT );
			Permission permissionWrite = new Permission( testSt.getInode(), roleAPI.loadCMSAnonymousRole().getId(), PermissionAPI.PERMISSION_WRITE );

			permissionAPI.save( permissionRead, testSt, user, false );
			permissionAPI.save( permissionEdit, testSt, user, false );
			permissionAPI.save( permissionWrite, testSt, user, false );

			// FIELDS

			// headline
			Field headline = new Field();
			headline.setFieldName("Headline");
			headline.setFieldType(FieldType.TEXT.toString());
			headline.setListed(true);
			headline.setRequired(true);
			headline.setSearchable(true);
			headline.setStructureInode(testSt.getInode());
			headline.setType("field");
			headline.setValues("");
			headline.setVelocityVarName("testTitle");
			headline.setIndexed(true);
			headline.setFieldContentlet("text4");
			FieldFactory.saveField( headline );

			// URL Title
			Field urlTitle = new Field();
			urlTitle.setFieldName("URL Title");
			urlTitle.setFieldType(FieldType.CUSTOM_FIELD.toString());
			urlTitle.setRequired(true);
			urlTitle.setStructureInode(testSt.getInode());
			urlTitle.setType("field");
			urlTitle.setValues("#dotParse('//shared/vtl/custom-fields/url-title.vtl')");
			urlTitle.setVelocityVarName("urlNewsTitle");
			urlTitle.setSearchable(true);
			urlTitle.setIndexed(true);
			urlTitle.setFieldContentlet("text_area4");
			FieldFactory.saveField( urlTitle );

			// WYSIWYG
			Field story = new Field();
			story.setFieldName("Story");
			story.setFieldType(FieldType.WYSIWYG.toString());
			story.setRequired(true);
			story.setStructureInode(testSt.getInode());
			story.setType("field");
			story.setValues("");
			story.setVelocityVarName("story");
			story.setFieldContentlet("text_area3");
			FieldFactory.saveField( story );

			// ENGLISH CONTENT
			Contentlet englishContent = new Contentlet();
			englishContent.setReviewInterval( "1m" );
			englishContent.setStructureInode( testSt.getInode() );
			englishContent.setHost( demoHost.getIdentifier() );
			englishContent.setLanguageId(1);


			contentletAPI.setContentletProperty( englishContent, headline, "the-gas-price" );
			contentletAPI.setContentletProperty( englishContent, story, "the-gas-price" );
			contentletAPI.setContentletProperty( englishContent, urlTitle, "the-gas-price" );
			//			englishContent.setStringProperty("urlNewsTitle", "the-gas-price");

			englishContent = contentletAPI.checkin( englishContent, null, permissionAPI.getPermissions( testSt ), user, false );
			APILocator.getVersionableAPI().setLive(englishContent);

			// SPANISH CONTENT
			Contentlet spanishContent = new Contentlet();
			spanishContent.setReviewInterval("1m");
			spanishContent.setStructureInode(testSt.getInode());
			spanishContent.setHost(demoHost.getIdentifier());
			spanishContent.setLanguageId(2);
			spanishContent.setIdentifier(englishContent.getIdentifier());

			contentletAPI.setContentletProperty( spanishContent, headline, "el-precio-del-gas" );
			contentletAPI.setContentletProperty( spanishContent, story, "el-precio-del-gas" );
			contentletAPI.setContentletProperty( spanishContent, urlTitle, "el-precio-del-gas" );
			//			spanishContent.setStringProperty("urlNewsTitle", "el-precio-del-gas");

			spanishContent = contentletAPI.checkin( spanishContent, null, permissionAPI.getPermissions( testSt ), user, false );
			APILocator.getVersionableAPI().setLive(spanishContent);

			HibernateUtil.commitTransaction();


			// TODO: make request to both pages

			if(contentletAPI.isInodeIndexed(englishContent.getInode(), true) &&
					contentletAPI.isInodeIndexed(spanishContent.getInode(), true) &&
						contentletAPI.isInodeIndexed(widget.getInode(), true)) {

				makeRequests(10);

			} else {
				fail("Content indexing timeout.");
			}

		} catch (Exception e) {
			try {
				HibernateUtil.rollbackTransaction();
			} catch (DotHibernateException e1) {
				Logger.error(PublisherAPIImpl.class,e.getMessage(),e1);
			}
			Logger.error(PublisherAPIImpl.class,e.getMessage(),e);
			throw e;
		} finally {
			if(testFolder!=null) APILocator.getFolderAPI().delete(testFolder, user, false);
			if(template!=null) APILocator.getTemplateAPI().delete(template, user, false);
			if(container!=null) APILocator.getContainerAPI().delete(container, user, false);
			if(testSt!=null) APILocator.getStructureAPI().delete(testSt, user);
			if(widget!=null) APILocator.getContentletAPI().delete(widget, user, false);
		}


	}

	private void makeRequests(int tries) throws ClientProtocolException, IOException {
		try {
			HttpServletRequest request = ServletTestRunner.localRequest.get();
			String serverName = request.getServerName();
			Integer serverPort = request.getServerPort();

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://"+serverName+":"+serverPort+"/newstest/the-gas-price");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);

			assertTrue(responseBody.contains("the-gas-price"));

			httpget = new HttpGet("http://"+serverName+":"+serverPort+"/newstest/el-precio-del-gas");
			responseBody = httpclient.execute(httpget, responseHandler);

			assertTrue(responseBody.contains("el-precio-del-gas"));
		} catch(HttpResponseException e) {
			if(tries>0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					Logger.error(this.getClass(), e1.getMessage());
				}
				makeRequests(tries--);
			}
			else
				throw e;
		}
	}


}
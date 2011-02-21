package com.google.enterprise.connector.diffing;

import java.util.LinkedList;

import com.google.enterprise.connector.db.DBConnectorConfig;
import com.google.enterprise.connector.db.DBException;
import com.google.enterprise.connector.db.DBTestBase;
import com.google.enterprise.connector.spi.RepositoryException;

public class RepositoryHandlerTest extends DBTestBase {

	
	
	public void testMakeRepositoryHandlerFromConfig() {
		try {
			DBConnectorConfig dbConnectorConfig=getDBConnectorConfig();
			RepositoryHandler repositoryHandler=RepositoryHandler.makeRepositoryHandlerFromConfig(dbConnectorConfig, null);
			assertNotNull(repositoryHandler);
		} catch (RepositoryException e) {
		System.out.println("Repository Exception");
		}
	}

	public void testSetCursorDB() {
		DBConnectorConfig dbConnectorConfig;
		try {
			int cursorDB=5;
			dbConnectorConfig = getDBConnectorConfig();
			RepositoryHandler repositoryHandler=RepositoryHandler.makeRepositoryHandlerFromConfig(dbConnectorConfig, null);
			repositoryHandler.setCursorDB(cursorDB);
			assertEquals(5, repositoryHandler.getCursorDB());
		} catch (RepositoryException e) {
					
			System.out.println("Repository Exception");
		}
		
	}

	public void testExecuteQueryAndAddDocs() {
		try {
			DBConnectorConfig dbConnectorConfig=getDBConnectorConfig();
			RepositoryHandler repositoryHandler=RepositoryHandler.makeRepositoryHandlerFromConfig(dbConnectorConfig, null);
			LinkedList<JsonDocument> jsonDocumenList=repositoryHandler.executeQueryAndAddDocs();
			assertEquals(true,jsonDocumenList.iterator().hasNext());
		} catch (RepositoryException e) {
			System.out.println("Repository Exception");
		}
		catch(DBException e){
			System.out.println("DB Exception");
		}
		
	}

}

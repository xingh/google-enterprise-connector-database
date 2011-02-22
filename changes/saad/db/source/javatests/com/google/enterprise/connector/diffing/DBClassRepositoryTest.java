package com.google.enterprise.connector.diffing;

import com.google.enterprise.connector.db.DBConnectorConfig;
import com.google.enterprise.connector.db.DBTestBase;


public class DBClassRepositoryTest extends DBTestBase {

	DBClassRepository  dbClassRepository;
	
	@Override
	protected void setUp() throws Exception {
	super.setUp();
	DBConnectorConfig dbConnectorConfig=getDBConnectorConfig();
	RepositoryHandler repositoryHandler=RepositoryHandler.makeRepositoryHandlerFromConfig(dbConnectorConfig, null);
	DBJsonDocumentFetcher dbJsonDocumentFetcher=new DBJsonDocumentFetcher(repositoryHandler);
	dbClassRepository=new DBClassRepository(dbJsonDocumentFetcher);
	}
	
	public void testIterator() {

		assertNotNull(dbClassRepository.iterator().hasNext());
		
	}

	public void testGetName() {
		String expected="com.google.enterprise.connector.diffing.DBClassRepository";
		String actual=dbClassRepository.getName();
		
		assertEquals(expected, actual);
	}

}

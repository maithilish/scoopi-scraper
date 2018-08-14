package org.codetab.scoopi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Date;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DocumentPersistence tests.
 * @author Maithilish
 *
 */
public class DocumentPersistenceTest {

    @Mock
    private ConfigService configService;

    @Mock
    private DaoFactoryProvider daoFactoryProvider;

    @Mock
    private JdoDaoFactory jdoDao;

    @Mock
    private IDocumentDao documentDao;

    @InjectMocks
    private DocumentPersistence documentPersistence;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoadDocumentById() {
        Document document = new ObjectFactory().createDocument("name", "url",
                new Date(), new Date());

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDocumentDao()).willReturn(documentDao);
        given(documentDao.getDocument(1L)).willReturn(document);

        Document actual = documentPersistence.loadDocument(1L);

        assertThat(actual).isSameAs(document);
        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, documentDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDocumentDao();
        inOrder.verify(documentDao).getDocument(1L);
        verifyNoMoreInteractions(configService, daoFactoryProvider, documentDao,
                jdoDao);
    }

    @Test
    public void testLoadDataByIdShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        documentPersistence.loadDocument(1L);
    }
}

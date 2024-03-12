//package iqadot.iqadotkit.business.impl;
//
//import iqadot.iqadotkit.persistence.UserRepository;
//import iqadot.iqadotkit.persistence.entity.UploadedArticle;
//import iqadot.iqadotkit.persistence.entity.UserEntity;
//import iqadot.iqadotkit.persistence.repository.UploadedArticleRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//
//@ContextConfiguration(classes = {ArticleServiceImpl.class})
//@ExtendWith(MockitoExtension.class)
//public class ArticleServiceImplTest {
//
//    @Mock
//    private UploadedArticleRepository uploadedArticleRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private WebClient.Builder webClientBuilder;
//
//    @InjectMocks
//    private ArticleServiceImpl articleService;
//
//    //    get all articles
//    @Test
//    void getAllArticlesWithArticles_Success() {
//        List<UploadedArticle> uploadedArticles = new ArrayList<>();
//        UploadedArticle uploadedArticle1 = new UploadedArticle();
//        uploadedArticle1.setArticleGuid("articleGuid1");
//        uploadedArticle1.setAdminId(new UserEntity());
//        uploadedArticle1.setPathFile("file1");
//        uploadedArticle1.setTitle("Title 1");
//        uploadedArticle1.setDateCreated(new Date());
//
//        UploadedArticle uploadedArticle2 = new UploadedArticle();
//        uploadedArticle2.setArticleGuid("articleGuid2");
//        uploadedArticle2.setAdminId(new UserEntity());
//        uploadedArticle2.setPathFile("file2");
//        uploadedArticle2.setTitle("Title 2");
//        uploadedArticle2.setDateCreated(new Date());
//
//        uploadedArticles.add(uploadedArticle1);
//        uploadedArticles.add(uploadedArticle2);
//        when(uploadedArticleRepository.findAll()).thenReturn(uploadedArticles);
//
//        // Execute the test
//        List<UploadedArticle> responses = articleService.getAllArticles();
//
//        // Assertions
//        assertNotNull(responses);
//        assertEquals(2, responses.size());
//        assertEquals("articleGuid1", responses.get(0).getArticleGuid());
//        assertEquals("Title 1", responses.get(0).getTitle());
//        assertEquals("articleGuid2", responses.get(1).getArticleGuid());
//        assertEquals("Title 2", responses.get(1).getTitle());
//    }
//
//    @Test
//    void getAllArticlesEmptyRepository_Success() {
//        List<UploadedArticle> uploadedArticles = new ArrayList<>();
//
//        when(uploadedArticleRepository.findAll()).thenReturn(uploadedArticles);
//
//
//        List<UploadedArticle> responses = articleService.getAllArticles();
//
//        assertNotNull(responses);
//        assertEquals(0, responses.size());
//    }
//
//}

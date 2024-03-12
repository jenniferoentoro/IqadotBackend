package iqadot.iqadotkit.business.impl;

import iqadot.iqadotkit.config.security.token.AccessToken;
import iqadot.iqadotkit.config.security.token.impl.AccessTokenImpl;
import iqadot.iqadotkit.controller.domain.CreateStaticItems;
import iqadot.iqadotkit.controller.domain.MultipleCreateStaticItems;
import iqadot.iqadotkit.controller.domain.UploadedStaticItemResponse;
import iqadot.iqadotkit.persistence.UserRepository;
import iqadot.iqadotkit.persistence.entity.UploadedStaticItem;
import iqadot.iqadotkit.persistence.repository.UploadedStaticItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StaticItemServiceImpl.class})
@ExtendWith(MockitoExtension.class)
class StaticItemServiceImplTest {
    @Mock
    private UploadedStaticItemRepository uploadedStaticItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private StaticItemServiceImpl staticItemService;

    @Test
    void getAllStaticItems_Successful() {
        List<UploadedStaticItem> expectedStaticItems = createMockStaticItems();
        when(uploadedStaticItemRepository.findAll()).thenReturn(expectedStaticItems);

        List<UploadedStaticItem> actualStaticItems = staticItemService.getAllStaticItems();

        assertEquals(expectedStaticItems.size(), actualStaticItems.size());
    }

    private List<UploadedStaticItem> createMockStaticItems() {
        List<UploadedStaticItem> staticItems = new ArrayList<>();

        // Create and add mock UploadedStaticItem objects to the list
        UploadedStaticItem item1 = new UploadedStaticItem();
        item1.setId(1L);
        item1.setSubject("Item 1");

        UploadedStaticItem item2 = new UploadedStaticItem();
        item2.setId(2L);
        item2.setSubject("Item 2");

        staticItems.add(item1);
        staticItems.add(item2);

        return staticItems;
    }

//    @Test
//    void createStaticItems_Successful() {
//        // Mock required dependencies and data
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        AccessToken accessToken = createMockAccessToken();
//        MultipleCreateStaticItems createStaticItemsList = createMockStaticItemsList();
//
//        request.addHeader("Authorization", "Bearer valid-token");
//
//        when(accessTokenEncoderDecoder.decode("valid-token")).thenReturn(accessToken);
//        when(userRepository.getById(accessToken.getUserId())).thenReturn(createMockUserEntity());
//        when(modelMapper.map(any(UploadedStaticItem.class), eq(UploadedStaticItemResponse.class))).thenReturn(createMockUploadedStaticItemResponse());
//        when(uploadedStaticItemRepository.save(any(UploadedStaticItem.class))).thenReturn(createMockUploadedStaticItem());
//
//        // Call the method you want to test
//        List<UploadedStaticItemResponse> uploadedStaticItemResponses = staticItemService.createStaticItems(createMockMultipleCreateStaticItems(), request);
//
//        // Assert the results
//        assertNotNull(uploadedStaticItemResponses);
//        // Add more assertions to verify the results
//    }
//
//    private MultipleCreateStaticItems createMockStaticItemsList() {
//        CreateStaticItems staticItem1 = new CreateStaticItems();
//        staticItem1.setBody("Sample Body 1");
//        staticItem1.setChannel("Sample Channel 1");
//        staticItem1.setSubject("Sample Subject 1");
//        staticItem1.setAnswer("Sample Answer 1");
//        staticItem1.setAuthor("Sample Author 1");
//        staticItem1.setTags("Sample Tags 1");
//        staticItem1.setRemarks("Sample Remarks 1");
//        staticItem1.setPublish(true);
//
//        CreateStaticItems staticItem2 = new CreateStaticItems();
//        staticItem2.setBody("Sample Body 2");
//        staticItem2.setChannel("Sample Channel 2");
//        staticItem2.setSubject("Sample Subject 2");
//        staticItem2.setAnswer("Sample Answer 2");
//        staticItem2.setAuthor("Sample Author 2");
//        staticItem2.setTags("Sample Tags 2");
//        staticItem2.setRemarks("Sample Remarks 2");
//        staticItem2.setPublish(false);
//
//        MultipleCreateStaticItems createStaticItemsList = new MultipleCreateStaticItems();
//        createStaticItemsList.getStaticItems().add(staticItem1);
//        createStaticItemsList.getStaticItems().add(staticItem2);
//
//        return createStaticItemsList;
//    }
//
//
//    private AccessToken createMockAccessToken() {
//        AccessToken accessToken = new AccessTokenImpl("test@example.com", 12345L);
//        return accessToken;
//    }



}
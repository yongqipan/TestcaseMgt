package com.pyq.tcm.ai.generation;

import com.pyq.tcm.ai.dto.AcceptRequest;
import com.pyq.tcm.ai.entity.AiGeneration;
import com.pyq.tcm.ai.entity.AiGenerationItem;
import com.pyq.tcm.ai.repository.AiGenerationItemRepository;
import com.pyq.tcm.ai.repository.AiGenerationRepository;
import com.pyq.tcm.common.BizException;
import com.pyq.tcm.entity.CaseStep;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiGenerationAcceptServiceTest {

    @Mock
    private AiGenerationRepository generationRepository;

    @Mock
    private AiGenerationItemRepository itemRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private ProjectRepository projectRepository;

    private AiGenerationAcceptService service;

    @BeforeEach
    void setUp() {
        service = new AiGenerationAcceptService(generationRepository, itemRepository,
                testCaseRepository, projectRepository);
    }

    private AiGeneration generation(String status) {
        AiGeneration generation = new AiGeneration();
        generation.setId(100L);
        generation.setProjectId(1L);
        generation.setModuleId(5L);
        generation.setStatus(status);
        generation.setAcceptedCount(0);
        return generation;
    }

    private AiGenerationItem item(long id, String title) {
        AiGenerationItem item = new AiGenerationItem();
        item.setId(id);
        item.setGenerationId(100L);
        item.setTitle(title);
        item.setPriority("P1");
        item.setType("FUNCTIONAL");
        item.setSteps(List.of(new CaseStep("step", "expected")));
        item.setStatus("PENDING");
        return item;
    }

    private Project project() {
        Project project = new Project();
        project.setId(1L);
        project.setCode("SHOP");
        return project;
    }

    @Test
    void acceptsAllItemsWhenNoSelection() {
        when(generationRepository.findById(100L)).thenReturn(Optional.of(generation("SUCCEEDED")));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(itemRepository.findByGenerationIdOrderByIdAsc(100L))
                .thenReturn(List.of(item(1, "case-a"), item(2, "case-b")));
        when(testCaseRepository.countByProjectId(1L)).thenReturn(0L, 1L);
        when(testCaseRepository.existsByCaseNo(anyString())).thenReturn(false);
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(inv -> {
            TestCase saved = inv.getArgument(0);
            saved.setId(System.nanoTime());
            return saved;
        });

        List<TestCase> created = service.accept(100L, null, "tester");

        assertEquals(2, created.size());
        ArgumentCaptor<TestCase> captor = ArgumentCaptor.forClass(TestCase.class);
        verify(testCaseRepository, times(2)).save(captor.capture());
        assertEquals("AI", captor.getAllValues().get(0).getSource());
        verify(generationRepository).save(any(AiGeneration.class));
    }

    @Test
    void acceptsOnlySelectedItems() {
        when(generationRepository.findById(100L)).thenReturn(Optional.of(generation("SUCCEEDED")));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(itemRepository.findByGenerationIdOrderByIdAsc(100L))
                .thenReturn(List.of(item(1, "case-a"), item(2, "case-b")));
        when(testCaseRepository.countByProjectId(1L)).thenReturn(0L);
        when(testCaseRepository.existsByCaseNo(anyString())).thenReturn(false);
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(inv -> {
            TestCase saved = inv.getArgument(0);
            saved.setId(System.nanoTime());
            return saved;
        });

        AcceptRequest request = new AcceptRequest();
        request.setItemIds(List.of(2L));

        List<TestCase> created = service.accept(100L, request, "tester");
        assertEquals(1, created.size());
        assertEquals("case-b", created.get(0).getTitle());
    }

    @Test
    void skipsAlreadyAcceptedItems() {
        AiGenerationItem accepted = item(1, "case-a");
        accepted.setAcceptedCaseId(999L);
        when(generationRepository.findById(100L)).thenReturn(Optional.of(generation("SUCCEEDED")));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(itemRepository.findByGenerationIdOrderByIdAsc(100L)).thenReturn(List.of(accepted));

        List<TestCase> created = service.accept(100L, null, "tester");
        assertEquals(0, created.size());
        verify(testCaseRepository, never()).save(any(TestCase.class));
    }

    @Test
    void rejectsUnfinishedGeneration() {
        when(generationRepository.findById(100L)).thenReturn(Optional.of(generation("RUNNING")));
        assertThrows(BizException.class, () -> service.accept(100L, null, "tester"));
    }

    @Test
    void rejectsMissingGeneration() {
        when(generationRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(BizException.class, () -> service.accept(404L, null, "tester"));
    }

    @Test
    void generatesSequentialCaseNumbers() {
        when(generationRepository.findById(100L)).thenReturn(Optional.of(generation("SUCCEEDED")));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(itemRepository.findByGenerationIdOrderByIdAsc(100L)).thenReturn(List.of(item(1, "case-a")));
        when(testCaseRepository.countByProjectId(1L)).thenReturn(2L);
        when(testCaseRepository.existsByCaseNo("TC-SHOP-003")).thenReturn(true);
        when(testCaseRepository.existsByCaseNo("TC-SHOP-004")).thenReturn(false);
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(inv -> {
            TestCase saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        List<TestCase> created = service.accept(100L, null, "tester");
        assertEquals("TC-SHOP-004", created.get(0).getCaseNo());
    }
}

package com.app.ChromaDress.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisFacadeTest {

    @Mock
    private WadaSanzoService wadaSanzoService;
    @Mock
    private ColorService colorService;

    @InjectMocks
    private AnalysisFacade analysisFacade;

    @Test
    @DisplayName("Should return WADA suggested palette.")
    void getSuggestedPalettesWadaType() {
        String hex = "#ffffff";
        List<List<String>> mockWada = List.of(List.of("#ff0000"), List.of("#00ff00", "#0000ff"));
        WadaColorDTO mockWadaDto = new WadaColorDTO(hex, "Field Blue", "#5c004d", mockWada);

        when(wadaSanzoService.getWadaPalette(hex)).thenReturn(mockWadaDto);

        List<List<String>> result = analysisFacade.getSuggestedPalette(hex, PaletteType.WADA);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(wadaSanzoService).getWadaPalette(hex);
        verifyNoInteractions(colorService);
    }

    @Test
    @DisplayName("Should return CLASSIC suggested palette.")
    void getSuggestedPalettesClassicType() {
        String hex = "#ffffff";
        List<List<String>> mockClassic = List.of(List.of("#ff0000"), List.of("#00ff00"), List.of("#0000ff"));

        ClassicColorDTO colorDTO = new ClassicColorDTO(mockClassic);
        when(colorService.getClassicPalette(hex)).thenReturn(colorDTO);

        List<List<String>> result = analysisFacade.getSuggestedPalette(hex, PaletteType.CLASSIC);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(colorService).getClassicPalette(hex);
        verifyNoInteractions(wadaSanzoService);
    }
}

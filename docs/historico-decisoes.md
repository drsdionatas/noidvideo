# Histórico de decisões

## 31/10/2026 — Export corrigido
- Problema: vídeo de 39s exportava com 2:47 travado.
- Causa: Composition estendia até a sequência de áudio (174s).
- Correção: cortar áudio na duração total dos vídeos.

## 31/10/2026 — Enquadramento
- Problema: 9:16 saía com barras pretas e imagem pequena.
- Causa: LAYOUT_SCALE_TO_FIT encaixa sem preencher.
- Correção: LAYOUT_SCALE_TO_FIT_WITH_CROP.

## 31/10/2026 — Volume
- Problema: sem controle de volume.
- Causa: GainProcessor não existe no Media3 1.5.0.
- Correção: VolumeAudioProcessor custom (BaseAudioProcessor).

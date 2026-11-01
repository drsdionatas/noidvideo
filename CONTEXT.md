# NoidVideo — Contexto Técnico

App Android de edição de vídeo (Kotlin + Jetpack Compose + Media3 1.5.0).
Pacote: com.noidvideo.editor

## Stack
- Kotlin, Jetpack Compose, Navigation Compose 2.8.4
- Media3 1.5.0 (transformer, effect, common, exoplayer, ui, decoder)
- Room 2.6.1
- JDK 17 (JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64)
- minSdk conforme app/build.gradle.kts

## Estrutura
- engine/     Timeline, TimelineClip, TimelineEngine, TransitionEngine, BeatDetector
- export/     VideoExporter, ExportConfig, VolumeAudioProcessor
- filters/    FilterPreviewRenderer
- ui/         NoidVideoApp, ProjectsScreen, MediaPickerScreen, EditorScreen,
              ExportScreen, PresetGallery, EditorViewModel, ExportViewModel
- data/       Room (projetos)

## Modelo de dados (TimelineClip)
id, uri, track (VIDEO|AUDIO), startInSourceMs, endInSourceMs, timelineStartMs,
speed (0.5|1|1.5|2), volume (0..2), filter (FilterPreset), transition (TransitionKind)

TransitionKind: NONE, CUT_ON_BEAT, CROSSFADE, FADE_BLACK, LIGHT_LEAK
FilterPreset: NONE, CINEMATIC, VIVID, WARM, COOL, BW, VINTAGE, SNOW, WATER

## Decisões técnicas importantes

### Export (VideoExporter.kt)
- Composition com 2 sequências: vídeo + áudio.
- O áudio é CORTADO na duração total dos vídeos. Sem isso, o Media3
  estende a composição até a maior sequência e o vídeo sai travado
  no último frame (bug observado: 39s de vídeo virou 2:47).
- Enquadramento usa Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP.
  LAYOUT_SCALE_TO_FIT deixava barras pretas enormes em 9:16.
- Effects(audioProcessors, videoEffects) — ÁUDIO PRIMEIRO. A ordem
  invertida não compila.
- Saída em MediaStore: Movies/NoidVideo, com IS_PENDING no Android Q+.

### Volume (VolumeAudioProcessor.kt)
- Media3 1.5.0 NÃO tem GainProcessor. Classes de áudio disponíveis em
  androidx.media3.common.audio: AudioProcessor, BaseAudioProcessor,
  SonicAudioProcessor, ChannelMixingAudioProcessor,
  SpeedChangingAudioProcessor, ToInt16PcmAudioProcessor.
- Solução: VolumeAudioProcessor extends BaseAudioProcessor, aplica ganho
  linear em PCM 16-bit.

### UI
- NoidVideoApp usa windowInsetsPadding(WindowInsets.safeDrawing) para o
  menu não ficar sob a barra de status.
- EditorViewModel é compartilhado entre telas (viewModel() no NavHost).

## Pendências conhecidas
1. Transição não é aplicada no export. O botão "Trans." só troca o nome
   no estado (cycleTransition). Falta aplicar CrossfadeEffect na Composition.
2. Filtros (FilterPreset) têm preview na UI mas não são aplicados no export.
3. Não há galeria visual de transições (só texto).
4. Velocidade (speed) não tem preview visual.

## Comandos
Build:  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug
Instalar: adb install -r app/build/outputs/apk/debug/NoidVideo-0.1.0-debug.apk
Sync:   ./sync.sh "mensagem do commit"

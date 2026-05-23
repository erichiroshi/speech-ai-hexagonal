const API_BASE = 'http://localhost:8080/api';

const $ = id => document.getElementById(id);

const els = {
  dropZone:           $('dropZone'),
  fileInput:          $('fileInput'),
  fileInfo:           $('fileInfo'),
  fileName:           $('fileName'),
  fileSize:           $('fileSize'),
  clearFile:          $('clearFile'),
  transcribeBtn:      $('transcribeBtn'),
  transcribeBtnText:  $('transcribeBtnText'),
  transcribeLoader:   $('transcribeLoader'),
  transcriptionResult:$('transcriptionResult'),
  transcriptionText:  $('transcriptionText'),
  cachedBadge:        $('cachedBadge'),
  hashDisplay:        $('hashDisplay'),
  copyText:           $('copyText'),
  analyzeBtn:         $('analyzeBtn'),
  analyzeBtnText:     $('analyzeBtnText'),
  analyzeLoader:      $('analyzeLoader'),
  analysisResult:     $('analysisResult'),
  analysisText:       $('analysisText'),
  modelBadge:         $('modelBadge'),
  analysisCachedBadge:$('analysisCachedBadge'),
  toast:              $('toast'),
};

let currentHash = null;
let selectedFile = null;

// ─── Drag & Drop ──────────────────────────────────────────────────────────────
els.dropZone.addEventListener('dragover', e => { e.preventDefault(); els.dropZone.classList.add('drag-over'); });
els.dropZone.addEventListener('dragleave', () => els.dropZone.classList.remove('drag-over'));
els.dropZone.addEventListener('drop', e => {
  e.preventDefault();
  els.dropZone.classList.remove('drag-over');
  const file = e.dataTransfer.files[0];
  if (file) setFile(file);
});
els.fileInput.addEventListener('change', () => {
  if (els.fileInput.files[0]) setFile(els.fileInput.files[0]);
});
els.clearFile.addEventListener('click', clearFile);

function setFile(file) {
  selectedFile = file;
  els.fileName.textContent = file.name;
  els.fileSize.textContent = formatSize(file.size);
  els.fileInfo.classList.remove('hidden');
  els.transcribeBtn.disabled = false;
  resetResults();
}

function clearFile() {
  selectedFile = null;
  els.fileInput.value = '';
  els.fileInfo.classList.add('hidden');
  els.transcribeBtn.disabled = true;
  resetResults();
}

function resetResults() {
  currentHash = null;
  els.transcriptionResult.classList.add('hidden');
  els.analyzeBtn.disabled = true;
  els.analysisResult.classList.add('hidden');
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
}

// ─── Transcrição ─────────────────────────────────────────────────────────────
els.transcribeBtn.addEventListener('click', async () => {
  if (!selectedFile) return;

  setLoading(els.transcribeBtn, els.transcribeBtnText, els.transcribeLoader, true, 'Transcrevendo...');
  els.transcriptionResult.classList.add('hidden');
  resetResults();

  const formData = new FormData();
  formData.append('file', selectedFile);

  try {
    const res = await fetch(`${API_BASE}/transcriptions`, { method: 'POST', body: formData });
    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.detail || data.title || `HTTP ${res.status}`);
    }

    els.transcriptionText.textContent = data.audioTranscription;
    els.hashDisplay.textContent = 'SHA-256: ' + data.audioHash;
    els.cachedBadge.style.display = data.cached ? 'inline-flex' : 'none';
    els.transcriptionResult.classList.remove('hidden');

    currentHash = data.audioHash;
    els.analyzeBtn.disabled = false;

    showToast(data.audioTranscription + (data.cached ? ' (cache hit)' : ''));
  } catch (err) {
    showToast('Erro: ' + err.message, true);
  } finally {
    setLoading(els.transcribeBtn, els.transcribeBtnText, els.transcribeLoader, false, 'Transcrever áudio');
  }
});

// ─── Análise ─────────────────────────────────────────────────────────────────
els.analyzeBtn.addEventListener('click', async () => {
  if (!currentHash) return;

  setLoading(els.analyzeBtn, els.analyzeBtnText, els.analyzeLoader, true, 'Analisando...');
  els.analysisResult.classList.add('hidden');

  try {
    const res = await fetch(`${API_BASE}/transcriptions/${currentHash}/analysis`, { method: 'POST' });
    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.detail || data.title || `HTTP ${res.status}`);
    }

    els.analysisText.textContent = data.summary || data.text || '';

    if (data.model) {
      els.modelBadge.textContent = data.model;
      els.modelBadge.style.display = 'inline-flex';
    }
    els.analysisCachedBadge.style.display = data.cached ? 'inline-flex' : 'none';
    els.analysisResult.classList.remove('hidden');

    showToast('Resumo gerado' + (data.cached ? ' (cache hit)' : ''));
  } catch (err) {
    showToast('Erro: ' + err.message, true);
  } finally {
    setLoading(els.analyzeBtn, els.analyzeBtnText, els.analyzeLoader, false, 'Gerar resumo');
  }
});

// ─── Copiar texto ─────────────────────────────────────────────────────────────
els.copyText.addEventListener('click', () => {
  const text = els.transcriptionText.textContent;
  if (!text) return;
  navigator.clipboard.writeText(text).then(() => showToast('Texto copiado!'));
});

// ─── Utilitários ──────────────────────────────────────────────────────────────
function setLoading(btn, textEl, loaderEl, loading, label) {
  btn.disabled = loading;
  textEl.textContent = label;
  loaderEl.classList.toggle('hidden', !loading);
}

let toastTimeout;
function showToast(msg, isError = false) {
  els.toast.textContent = msg;
  els.toast.style.borderColor = isError ? '#ef4444' : '';
  els.toast.classList.remove('hidden');
  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => els.toast.classList.add('hidden'), 3000);
}

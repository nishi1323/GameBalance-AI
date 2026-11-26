import { useEffect, useRef, useState } from 'react';
import { Bot, Send, Sparkles, Trash2 } from 'lucide-react';
import { chatApi, extractErrorMessage } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';

const QUICK_PROMPTS = [
  'How can I stop playing "one more match" before bed?',
  'Help me build a balanced evening study and gaming routine.',
  'How do I stay focused on assignments without burning out?',
  'Tips to recover my sleep schedule after late-night gaming.',
];

export default function WellnessAssistantPage() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const bottomRef = useRef(null);

  useEffect(() => {
    chatApi
      .getHistory()
      .then((hist) => setMessages(Array.isArray(hist) ? hist : []))
      .catch((err) => setError(extractErrorMessage(err, 'Unable to load chat history.')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, sending]);

  const sendText = async (text) => {
    const trimmed = text.trim();
    if (!trimmed || sending) return;

    setError('');
    setInput('');
    const optimisticUser = {
      id: `temp-${Date.now()}`,
      role: 'user',
      content: trimmed,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, optimisticUser]);
    setSending(true);

    try {
      const savedPair = await chatApi.sendMessage(trimmed);
      setMessages((prev) => [...prev.filter((m) => m.id !== optimisticUser.id), ...savedPair]);
    } catch (err) {
      setMessages((prev) => prev.filter((m) => m.id !== optimisticUser.id));
      setError(extractErrorMessage(err, 'Failed to get a response from AI Assistant.'));
    } finally {
      setSending(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendText(input);
  };

  const handleClear = async () => {
    try {
      await chatApi.clearHistory();
      setMessages([]);
      setError('');
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to clear conversation.'));
    }
  };

  if (loading) return <LoadingSpinner text="Opening AI Wellness Assistant..." />;

  return (
    <div className="flex-col gap-3">
      <div className="alert alert-info mb-2">
        <Bot size={20} color="var(--accent)" style={{ flexShrink: 0 }} />
        <div className="text-sm">
          <strong>Friendly Student Wellness Coach:</strong> Powered by Groq AI with awareness of your gaming, study, and sleep progress.
          Not a medical service—if you experience severe distress, please reach out to a campus counselor or healthcare professional.
        </div>
      </div>

      {error && <div className="alert alert-danger mb-2">{error}</div>}

      <div className="card chat-box" style={{ padding: 0, overflow: 'hidden' }}>
        <div
          className="card-header"
          style={{ padding: '1rem 1.25rem', marginBottom: 0, borderBottom: '1px solid var(--border-color)' }}
        >
          <div className="flex items-center gap-2">
            <Sparkles size={18} color="var(--accent)" />
            <span className="font-semibold">GameBalance AI Wellness Chat</span>
          </div>
          {messages.length > 0 && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={handleClear}>
              <Trash2 size={14} />
              <span>Clear Conversation</span>
            </button>
          )}
        </div>

        <div className="chat-messages">
          {messages.length === 0 ? (
            <div className="empty-state" style={{ margin: 'auto 0' }}>
              <Bot size={36} color="var(--accent)" style={{ marginBottom: '0.6rem' }} />
              <div className="font-semibold" style={{ color: 'var(--text-primary)', marginBottom: '0.35rem' }}>
                Hi! I'm your GameBalance AI Wellness Assistant.
              </div>
              <p className="text-sm text-muted mb-3">
                Ask me anything about reducing excessive gaming, managing study deadlines, improving sleep, or staying active.
              </p>
              <div className="flex items-center gap-2" style={{ flexWrap: 'wrap', justifyContent: 'center' }}>
                {QUICK_PROMPTS.map((q) => (
                  <button
                    key={q}
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => sendText(q)}
                  >
                    {q}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            messages.map((msg) => (
              <div key={msg.id} className={`chat-bubble ${msg.role}`}>
                <div>{msg.content}</div>
                <div
                  className="text-xs"
                  style={{
                    marginTop: '0.35rem',
                    opacity: 0.7,
                    textAlign: msg.role === 'user' ? 'right' : 'left',
                  }}
                >
                  {msg.role === 'user' ? 'You' : 'GameBalance AI'} ·{' '}
                  {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  {msg.role === 'assistant' && msg.aiPowered === false && ' · Offline Fallback'}
                </div>
              </div>
            ))
          )}

          {sending && (
            <div className="chat-bubble assistant">
              <div className="flex items-center gap-2 text-sm text-muted">
                <div className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} />
                <span>Thinking of helpful wellness suggestions...</span>
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <form
          onSubmit={handleSubmit}
          className="flex items-center gap-2"
          style={{ padding: '0.9rem 1.25rem', borderTop: '1px solid var(--border-color)', background: 'var(--bg-surface-alt)' }}
        >
          <input
            type="text"
            className="form-input"
            placeholder="Ask about gaming limits, study focus, sleep, or stress..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={sending}
          />
          <button type="submit" className="btn btn-primary" disabled={sending || !input.trim()}>
            <Send size={16} />
            <span>Send</span>
          </button>
        </form>
      </div>
    </div>
  );
}

import { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';

const WebSocketContext = createContext(null);

const HEARTBEAT_INTERVAL_MS = 30000;
const HEARTBEAT_TIMEOUT_MS = 10000;
const MAX_RECONNECT_MS = 30000;

export function WebSocketProvider({ children }) {
  const [status, setStatus] = useState('connecting'); // connecting | connected | reconnecting | disconnected
  const [lastMessage, setLastMessage] = useState(null);
  const [reconnectAttempt, setReconnectAttempt] = useState(0);
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const heartbeatIntervalRef = useRef(null);
  const lastPongRef = useRef(0);
  const reconnectAttemptRef = useRef(0);
  const manuallyClosedRef = useRef(false);
  const listenersRef = useRef(new Map());
  const bufferRef = useRef(new Map());

  const connected = status === 'connected';

  const scheduleReconnect = useCallback((delay) => {
    if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
    setStatus('reconnecting');
    setReconnectAttempt(reconnectAttemptRef.current);
    reconnectTimeoutRef.current = setTimeout(() => {
      connectRef.current();
    }, delay);
  }, []);

  const startHeartbeat = useCallback((ws) => {
    if (heartbeatIntervalRef.current) clearInterval(heartbeatIntervalRef.current);
    lastPongRef.current = Date.now();
    heartbeatIntervalRef.current = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send('PING');
        // Force-reconnect if the server never answered our last PING.
        if (Date.now() - lastPongRef.current > HEARTBEAT_TIMEOUT_MS + HEARTBEAT_INTERVAL_MS) {
          ws.close();
        }
      }
    }, HEARTBEAT_INTERVAL_MS);
  }, []);

  const connect = useCallback(() => {
    manuallyClosedRef.current = false;
    if (wsRef.current?.readyState === WebSocket.OPEN) return;
    if (wsRef.current?.readyState === WebSocket.CONNECTING) return;

    setStatus('connecting');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // Use the current host; fall back to port 8080 when the UI runs on the
    // Vite dev server (port 5173). Works on localhost, LAN and production
    // deployments served from the same origin.
    const port = window.location.port
    const host = port === '5173' ? `${window.location.hostname}:8080` : window.location.host
    const ws = new WebSocket(`${protocol}//${host}/ws/live`);
    wsRef.current = ws;

    ws.onopen = () => {
      reconnectAttemptRef.current = 0;
      setReconnectAttempt(0);
      setStatus('connected');
      startHeartbeat(ws);
    };

    ws.onmessage = (event) => {
      try {
        const raw = event.data;
        if (raw === 'PONG') {
          lastPongRef.current = Date.now();
          return;
        }
        const data = JSON.parse(raw);
        setLastMessage(data);
        if (data.type !== undefined && data.data !== undefined) {
          bufferRef.current.set(data.type, data.data);
        }
        const listeners = listenersRef.current.get(data.type);
        if (listeners) listeners.forEach(cb => cb(data.data));
        const allListeners = listenersRef.current.get('*');
        if (allListeners) allListeners.forEach(cb => cb(data));
      } catch (e) {
        console.error('WebSocket message parse error:', e);
      }
    };

    ws.onclose = () => {
      if (heartbeatIntervalRef.current) clearInterval(heartbeatIntervalRef.current);
      wsRef.current = null;
      if (manuallyClosedRef.current) {
        setStatus('disconnected');
        return;
      }
      const attempt = reconnectAttemptRef.current;
      const delay = Math.min(1000 * Math.pow(2, attempt), MAX_RECONNECT_MS);
      reconnectAttemptRef.current = attempt + 1;
      scheduleReconnect(delay);
    };

    ws.onerror = () => {
      ws.close();
    };
  }, [scheduleReconnect, startHeartbeat]);

  const connectRef = useRef(connect);
  useEffect(() => { connectRef.current = connect; }, [connect]);

  useEffect(() => {
    connect();
    return () => {
      manuallyClosedRef.current = true;
      if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
      if (heartbeatIntervalRef.current) clearInterval(heartbeatIntervalRef.current);
      if (wsRef.current) wsRef.current.close();
    };
  }, [connect]);

  const subscribe = useCallback((type, callback) => {
    if (!listenersRef.current.has(type)) {
      listenersRef.current.set(type, new Set());
    }
    listenersRef.current.get(type).add(callback);
    // Immediately replay the last buffered payload for this type, if any.
    const buffered = bufferRef.current.get(type);
    if (buffered !== undefined) callback(buffered);
    return () => {
      const typeListeners = listenersRef.current.get(type);
      if (typeListeners) {
        typeListeners.delete(callback);
        if (typeListeners.size === 0) listenersRef.current.delete(type);
      }
    };
  }, []);

  const send = useCallback((payload) => {
    const ws = wsRef.current;
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(typeof payload === 'string' ? payload : JSON.stringify(payload));
      return true;
    }
    return false;
  }, []);

  return (
    <WebSocketContext.Provider value={{ connected, status, lastMessage, reconnectAttempt, subscribe, send }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export const useWebSocket = () => useContext(WebSocketContext);

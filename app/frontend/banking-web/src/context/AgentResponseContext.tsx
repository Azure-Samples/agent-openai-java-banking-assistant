import { createContext, useContext, useCallback, useRef, useMemo, ReactNode, useEffect } from "react";

const AgentResponseContext = createContext<{
  registerOnResponseEnd: (handler: () => void) => () => void;
  triggerOnResponseEnd: () => void;
} | null>(null);

export const AgentResponseProvider = ({ children }: { children: ReactNode }) => {
  const handlerRef = useRef<(() => void) | null>(null);

  const registerOnResponseEnd = useCallback((handler: () => void) => {
    handlerRef.current = handler;
    return () => {
      if (handlerRef.current === handler) {
        handlerRef.current = null;
      }
    };
  }, []);

  const triggerOnResponseEnd = useCallback(() => {
    handlerRef.current?.();
  }, []);

  const value = useMemo(
    () => ({ registerOnResponseEnd, triggerOnResponseEnd }),
    [registerOnResponseEnd, triggerOnResponseEnd]
  );

  return (
    <AgentResponseContext.Provider value={value}>
      {children}
    </AgentResponseContext.Provider>
  );
};

export const useAgentResponse = () => {
  const context = useContext(AgentResponseContext);
  if (!context) {
    throw new Error("useAgentResponse must be used within an AgentResponseProvider");
  }
  return context;
};

export const useAgentResponseHandler = (handler: () => void) => {
  const { registerOnResponseEnd } = useAgentResponse();

  useEffect(() => {
    const cleanup = registerOnResponseEnd(handler);
    return cleanup;
  }, [handler, registerOnResponseEnd]);
};

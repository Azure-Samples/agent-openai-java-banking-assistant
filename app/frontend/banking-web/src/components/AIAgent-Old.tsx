
import { useState, useRef, useEffect } from "react";
import { MessageCircle, Send, X, Minimize2, Maximize2, Bot, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

interface Message {
  id: string;
  text: string;
  sender: 'user' | 'agent';
  timestamp: Date;
}

const suggestedActions = [
  "Show my account balance",
  "Pay electricity bill",
  "Analyze spending patterns",
  "Schedule recurring payment",
  "Show recent transactions",
  "Help with budget planning"
];

export default function AIAgent() {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [showInvitation, setShowInvitation] = useState(true);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      setTimeout(() => {
        setMessages([{
          id: '1',
          text: "Hello! I'm your AI Banking Assistant. I can help you manage accounts, pay bills, analyze transactions, and provide financial insights. What would you like to do today?",
          sender: 'agent',
          timestamp: new Date()
        }]);
      }, 500);
    }
  }, [isOpen]);

  useEffect(() => {
    const timer = setTimeout(() => setShowInvitation(false), 10000);
    return () => clearTimeout(timer);
  }, []);

  const handleSendMessage = async (text: string) => {
    if (!text.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      text,
      sender: 'user',
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputValue("");
    setIsTyping(true);

    setTimeout(() => {
      const responses = [
        "I can help you with that! Let me pull up your account information...",
        "Certainly! I'll process that payment for you. Please review the details...",
        "Based on your spending patterns, I've identified some valuable insights...",
        "I've found your recent transactions. Would you like me to categorize them?",
        "Your current account balance is $12,459.32. Is there anything specific you'd like to know?",
        "I can set up that recurring payment for you. What frequency would you prefer?"
      ];

      const agentMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: responses[Math.floor(Math.random() * responses.length)],
        sender: 'agent',
        timestamp: new Date()
      };

      setMessages(prev => [...prev, agentMessage]);
      setIsTyping(false);
    }, 1500);
  };

  const handleSuggestedAction = (action: string) => {
    handleSendMessage(action);
  };

  if (!isOpen) {
    return (
      <div className="fixed bottom-6 right-6 z-50">
        {/* Invitation Card */}
        {showInvitation && (
          <div className="mb-4 mr-4 animate-slide-up">
            <Card className="bg-white border border-slate-200 shadow-professional-lg p-4 w-72">
              <div className="flex items-start space-x-3">
                <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                  <Sparkles className="h-4 w-4 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-sm font-semibold text-slate-900 mb-1">AI Assistant Ready</h3>
                  <p className="text-xs text-slate-600 mb-2">Need help with banking tasks? I can assist with payments, account info, and more.</p>
                  <Button 
                    size="sm" 
                    onClick={() => {
                      setIsOpen(true);
                      setShowInvitation(false);
                    }}
                    className="text-xs h-7 bg-blue-600 hover:bg-blue-700"
                  >
                    Start Chat
                  </Button>
                </div>
                <button
                  onClick={() => setShowInvitation(false)}
                  className="text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            </Card>
          </div>
        )}

        {/* Chat Button */}
        <Button
          onClick={() => setIsOpen(true)}
          className="h-14 w-14 rounded-full bg-blue-600 hover:bg-blue-700 shadow-professional-lg animate-bounce-gentle"
          size="icon"
        >
          <MessageCircle className="h-6 w-6" />
        </Button>
      </div>
    );
  }

  return (
    <Card className={`fixed bottom-6 right-6 bg-white border border-slate-200 shadow-professional-lg transition-all duration-300 animate-scale-in z-50 ${
      isMinimized ? "h-16 w-80" : "h-[500px] w-96"
    }`}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-slate-200 bg-gradient-to-r from-blue-50 to-indigo-50">
        <div className="flex items-center space-x-3">
          <div className="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center">
            <Bot className="h-5 w-5 text-white" />
          </div>
          <div>
            <h3 className="font-semibold text-sm text-slate-900">AI Banking Assistant</h3>
            <p className="text-xs text-slate-600 flex items-center">
              <span className="w-2 h-2 bg-green-500 rounded-full mr-1"></span>
              Online & Ready to Help
            </p>
          </div>
        </div>
        <div className="flex items-center space-x-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsMinimized(!isMinimized)}
            className="h-8 w-8 hover:bg-slate-100"
          >
            {isMinimized ? <Maximize2 className="h-4 w-4" /> : <Minimize2 className="h-4 w-4" />}
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsOpen(false)}
            className="h-8 w-8 hover:bg-slate-100"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {!isMinimized && (
        <>
          {/* Messages */}
          <div className="h-80 overflow-y-auto p-4 space-y-4 bg-slate-50">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[80%] p-3 rounded-lg text-sm ${
                    message.sender === 'user'
                      ? 'bg-blue-600 text-white'
                      : 'bg-white text-slate-700 border border-slate-200 shadow-sm'
                  }`}
                >
                  {message.text}
                </div>
              </div>
            ))}
            
            {isTyping && (
              <div className="flex justify-start">
                <div className="bg-white text-slate-700 border border-slate-200 p-3 rounded-lg text-sm shadow-sm">
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style={{animationDelay: '0.1s'}}></div>
                    <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style={{animationDelay: '0.2s'}}></div>
                  </div>
                </div>
              </div>
            )}

            {/* Suggested Actions */}
            {messages.length === 1 && (
              <div className="space-y-3">
                <p className="text-xs text-slate-600 font-medium">Quick Actions:</p>
                <div className="grid grid-cols-1 gap-2">
                  {suggestedActions.slice(0, 3).map((action, index) => (
                    <Button
                      key={index}
                      variant="outline"
                      size="sm"
                      onClick={() => handleSuggestedAction(action)}
                      className="text-xs h-8 justify-start text-slate-700 border-slate-200 hover:bg-slate-50"
                    >
                      {action}
                    </Button>
                  ))}
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="p-4 border-t border-slate-200 bg-white">
            <div className="flex space-x-2">
              <Input
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                placeholder="Type your message..."
                onKeyPress={(e) => e.key === 'Enter' && handleSendMessage(inputValue)}
                className="flex-1 border-slate-200 focus:border-blue-500 focus:ring-blue-200"
              />
              <Button
                onClick={() => handleSendMessage(inputValue)}
                size="icon"
                className="bg-blue-600 hover:bg-blue-700"
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </>
      )}
    </Card>
  );
}

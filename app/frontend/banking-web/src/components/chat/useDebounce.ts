import { useEffect, useState } from "react";

/**
 * Debounces a value, updating it only after the specified delay has passed
 * without the value changing. Creates a typewriter effect for streaming text.
 * 
 * @param value - The value to debounce
 * @param delay - The delay in milliseconds
 *   - 0ms: No delay, instant update (use when streaming is complete)
 *   - 50-100ms: Smooth updates, minimal typewriter effect
 *   - 150-300ms: Visible typewriter effect, good for UX feedback
 * @returns The debounced value
 */
export function useDebounce<T>(value: T, delay: number = 50): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // If delay is 0, update immediately (for when streaming completes)
    if (delay === 0) {
      setDebouncedValue(value);
      return;
    }

    // Set up a timer to update the debounced value after the delay
    // This creates the typewriter effect by batching rapid updates
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Clean up the timer if value changes before delay expires
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

import { useState, useEffect, useRef } from 'react';
import { Typography } from '@mui/material';

export default function AnimatedCounter({ value, duration = 1000, variant = 'h3', sx = {}, separator = true }) {
  const [count, setCount] = useState(0);
  const prevValueRef = useRef(0);
  const frameRef = useRef(null);

  useEffect(() => {
    let startTime = null;
    const startValue = prevValueRef.current;
    const endValue = value;
    prevValueRef.current = value;

    if (startValue === endValue) {
      setCount(endValue);
      return () => { if (frameRef.current) cancelAnimationFrame(frameRef.current) };
    }

    const animate = (timestamp) => {
      if (!startTime) startTime = timestamp;
      const progress = Math.min((timestamp - startTime) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setCount(Math.floor(startValue + (endValue - startValue) * eased));
      if (progress < 1) frameRef.current = requestAnimationFrame(animate);
    };

    frameRef.current = requestAnimationFrame(animate);
    return () => { if (frameRef.current) cancelAnimationFrame(frameRef.current) };
  }, [value, duration]);

  return (
    <Typography variant={variant} aria-live="polite" aria-atomic="true" sx={sx}>
      {separator ? count.toLocaleString() : count}
    </Typography>
  );
}

import { useCallback, useEffect, useRef, useState } from 'react';
import { studyApi } from '../services/api';

export function useStudyReminders(enabled) {
  const [reminders, setReminders] = useState([]);
  const [permission, setPermission] = useState(
    typeof window !== 'undefined' && 'Notification' in window ? Notification.permission : 'unsupported'
  );
  const notifiedIdsRef = useRef(new Set());

  const requestPermission = useCallback(async () => {
    if (!('Notification' in window)) {
      setPermission('unsupported');
      return 'unsupported';
    }
    const result = await Notification.requestPermission();
    setPermission(result);
    return result;
  }, []);

  const checkReminders = useCallback(async () => {
    if (!enabled) return;
    try {
      const summary = await studyApi.getSummary();
      const upcoming = summary.upcomingReminders || [];
      setReminders(upcoming);

      if ('Notification' in window && Notification.permission === 'granted') {
        const now = Date.now();
        upcoming.forEach((task) => {
          const dueMs = new Date(task.dueDateTime).getTime();
          const diffMins = Math.round((dueMs - now) / 60000);
          if (diffMins <= 60 && diffMins >= -15 && !notifiedIdsRef.current.has(task.id)) {
            notifiedIdsRef.current.add(task.id);
            new Notification(`Study Reminder: ${task.title}`, {
              body: `${task.subjectName} is due ${diffMins <= 0 ? 'now' : `in ${diffMins} min(s)`}. Stay balanced!`,
            });
          }
        });
      }
    } catch {
      // Silently ignore background reminder polling errors
    }
  }, [enabled]);

  useEffect(() => {
    if (!enabled) return;
    checkReminders();
    const interval = setInterval(checkReminders, 60000);
    return () => clearInterval(interval);
  }, [enabled, checkReminders]);

  return { reminders, permission, requestPermission, refreshReminders: checkReminders };
}

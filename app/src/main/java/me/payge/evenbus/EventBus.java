package me.payge.evenbus;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class EventBus implements Handler.Callback, GenericLifecycleObserver {

    /*
     * In newer class files, compilers may add methods. Those are called bridge or synthetic methods.
     * EventBus must ignore both. There modifiers are not public but defined in the Java class file format:
     * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    private static volatile EventBus bus;

    private Map<String, List<ObserverMethod>> observerCenter = new LinkedHashMap<>();

    private Handler mainHandler;
    private ExecutorService service;

    private EventBus() {
        mainHandler = new Handler(Looper.getMainLooper(), this);
        service = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 3));
    }

    public static EventBus getInstance() {
        if (bus == null) {
            synchronized (EventBus.class) {
                if (bus == null) {
                    bus = new EventBus();
                }
            }
        }
        return bus;
    }

    public void register(Object observer) {
        if (observer == null) return;
        String hash = Integer.toHexString(observer.hashCode());
        if (observerCenter.containsKey(hash)) return;
        if (observer instanceof LifecycleOwner) {
            ((LifecycleOwner)observer).getLifecycle().addObserver(this);
        }
        Class<?> clazz = observer.getClass();
        Method[] methods;
        try {
            // This is faster than getMethods, especially when subscribers are fat classes like Activities
            methods = clazz.getDeclaredMethods();
        } catch (Throwable th) {
            methods = clazz.getMethods();
        }
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    EventObserver eventObserverAnnotation = method.getAnnotation(EventObserver.class);
                    if (eventObserverAnnotation != null) {
                        Class<?> eventType = parameterTypes[0];
                        int runThread = eventObserverAnnotation.runThread();
                        List<ObserverMethod> observerMethods = observerCenter.get(hash);
                        if (observerMethods == null) {
                            observerMethods = new ArrayList<>();
                            observerCenter.put(hash, observerMethods);
                        }
                        observerMethods.add(new ObserverMethod(observer, eventType, runThread, method));
                    }
                } else if (method.isAnnotationPresent(EventObserver.class)) {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new IllegalStateException("@Subscribe method " + methodName +
                            "must have exactly 1 parameter but has " + parameterTypes.length);
                }
            } else if (method.isAnnotationPresent(EventObserver.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new IllegalStateException(methodName +
                        " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
            }
        }
    }

    public void unregister(Object o) {
        if (o == null) return;
        String hash = Integer.toHexString(o.hashCode());
        observerCenter.remove(hash) ;
    }

    public void post(Object event) {
        Class<?> eventType = event.getClass();
        Collection<List<ObserverMethod>> col = observerCenter.values();
        if (col.isEmpty()) return;
        for (List<ObserverMethod> list : col) {
            for (ObserverMethod om : list) {
                if (!om.eventType.isAssignableFrom(eventType)) {
                    continue;
                }
                om.event = event;
                if (om.runThread == EventObserver.RunThread.CURRENT) {
                    invokeMethod(om);
                } else if (om.runThread == EventObserver.RunThread.MAIN) {
                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        invokeMethod(om);
                    } else {
                        mainHandler.obtainMessage(1, om).sendToTarget();
                    }
                } else if (om.runThread == EventObserver.RunThread.ASYNC) {
                    asyncInvoke(om);
                }
            }
        }
    }

    private void invokeMethod(ObserverMethod om) {
        try {
            om.method.invoke(om.observer, om.event);
            om.event = null;
        } catch (IllegalAccessException e) {
            Log.e("xxx", "", e);
        } catch (InvocationTargetException e) {
            Log.e("xxx", "", e);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        ObserverMethod om = (ObserverMethod) msg.obj;
        invokeMethod(om);
        return true;
    }

    private void asyncInvoke(final ObserverMethod om) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                invokeMethod(om);
            }
        });
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            source.getLifecycle().removeObserver(this);
            String key = Integer.toHexString(source.hashCode());
            String name = source.getClass().getSimpleName();
            Log.i("xxx", String.format("unregister before: %s subscribers %s", name, observerCenter.get(key)));
            unregister(source);
            Log.i("xxx", String.format("unregister after: %s subscribers %s", name, observerCenter.get(key)));
        }
    }

    private static final class ObserverMethod {
        Object observer;
        Class<?> eventType;
        int runThread;
        Method method;
        Object event;

        ObserverMethod(Object observer, Class<?> eventType, int runThread, Method method) {
            this.observer = observer;
            this.eventType = eventType;
            this.runThread = runThread;
            this.method = method;
        }

        @Override
        public String toString() {
            return method.toGenericString();
        }
    }
}

package com.eastrobot.robotdev.common;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * ProxyInvoker
 *
 * @Author alan.peng
 * @Date 2017-09-11 15:47
 */
public class ProxyInvoker {

    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final ClassLoader LOCAL_LOADER = ProxyInvoker.class.getClassLoader();

    public ProxyInvoker() {
    }

    public static Object invokeTarget(Object target, MethodDefinition method) {
        return invokeTarget(target, method, EMPTY_ARGS);
    }

    public static Object invokeTarget(Object target, MethodDefinition methodDefinition, Object[] localArgs) {
        try {
            Class targetClass = target.getClass();
            ClassLoader remoteLoader = targetClass.getClassLoader();
            Object[] remoteArgs = new Object[localArgs.length];

            Object remoteReturn;
            Object localReturn;
            for(int i = 0; i < localArgs.length; ++i) {
                remoteReturn = localArgs[i];
                localReturn = newProxy(remoteLoader, remoteReturn);
                remoteArgs[i] = localReturn;
            }

            Method method = getMethod(remoteLoader, targetClass, methodDefinition);
            remoteReturn = method.invoke(target, remoteArgs);
            localReturn = newProxy(LOCAL_LOADER, remoteReturn);
            return localReturn;
        } catch (Exception var9) {
            throw new RuntimeException(var9.getMessage(), var9);
        }
    }

    public static Method getMethod(ClassLoader targetLoader, Class targetClass, MethodDefinition methodDefinition) {
        return getMethod(targetLoader, targetClass, methodDefinition, false);
    }

    public static Method getMethod(ClassLoader targetLoader, Class targetClass, MethodDefinition methodDefinition, boolean retry0) {
        boolean retry = false;
        String[] argClassNames = methodDefinition.getArgClassNames();
        Class[] argTypes = new Class[argClassNames.length];

        try {
            for(int i = 0; i < argClassNames.length; ++i) {
                if(argClassNames[i].equals("java.lang.Integer")) {
                    if(retry0) {
                        argTypes[i] = Integer.TYPE;
                        continue;
                    }

                    retry = true;
                }

                if(argClassNames[i].equals("java.lang.Long")) {
                    if(retry0) {
                        argTypes[i] = Long.TYPE;
                        continue;
                    }

                    retry = true;
                }

                if(argClassNames[i].startsWith("[L")) {
                    String componentType = argClassNames[i].substring(2, argClassNames[i].length() - 1);
                    Object obj = Array.newInstance(targetLoader.loadClass(componentType), 0);
                    argTypes[i] = obj.getClass();
                } else {
                    argTypes[i] = targetLoader.loadClass(argClassNames[i]);
                }
            }

            return targetClass.getMethod(methodDefinition.getMethodName(), argTypes);
        } catch (NoSuchMethodException var10) {
            if(retry) {
                return getMethod(targetLoader, targetClass, methodDefinition, true);
            } else {
                var10.printStackTrace();
                return null;
            }
        } catch (Exception var11) {
            var11.printStackTrace();
            return null;
        }
    }

    private static Object newProxy(ClassLoader loader, Object target) throws Exception {
        if(target == null) {
            return null;
        } else {
            Object proxy = null;
            Class targetClass = target.getClass();
            String targetClassName = targetClass.getName();
            Object[] targetArray;
            int i;
            if(targetClass.isArray()) {
                Object[] array = (Object[])target;
                targetArray = (Object[])Array.newInstance(loader.loadClass(array.getClass().getComponentType().getName()), array.length);

                for(i = 0; i < array.length; ++i) {
                    targetArray[i] = newProxy(loader, array[i]);
                }

                proxy = targetArray;
            } else if(targetClassName.startsWith("java.")) {
                Object targetMap;
                if(List.class.isAssignableFrom(targetClass)) {
                    List list = (List)target;
                    targetArray = null;

                    try {
                        targetMap = (List)list.getClass().newInstance();
                    } catch (Throwable var11) {
                        targetMap = new ArrayList();
                    }

                    for(i = 0; i < list.size(); ++i) {
                        ((List)targetMap).add(i, newProxy(loader, list.get(i)));
                    }

                    proxy = targetMap;
                } else {
                    Iterator var8;
                    if(Set.class.isAssignableFrom(targetClass)) {
                        Set set = (Set)target;
                        targetArray = null;

                        try {
                            targetMap = (Set)set.getClass().newInstance();
                        } catch (Throwable var10) {
                            targetMap = new HashSet();
                        }

                        var8 = set.iterator();

                        while(var8.hasNext()) {
                            Object o = var8.next();
                            ((Set)targetMap).add(newProxy(loader, o));
                        }

                        proxy = targetMap;
                    } else if(Map.class.isAssignableFrom(targetClass)) {
                        Map<Object, Object> map = (Map)target;
                        targetArray = null;

                        try {
                            targetMap = (Map)map.getClass().newInstance();
                        } catch (Throwable var9) {
                            targetMap = new HashMap();
                        }

                        var8 = map.entrySet().iterator();

                        while(var8.hasNext()) {
                            Map.Entry entry = (Map.Entry)var8.next();
                            ((Map)targetMap).put(newProxy(loader, entry.getKey()), newProxy(loader, entry.getValue()));
                        }

                        proxy = targetMap;
                    } else {
                        proxy = target;
                    }
                }
            } else if(targetClassName.endsWith("Proxy")) {
                proxy = loader.loadClass(targetClassName).getConstructor(new Class[]{Object.class}).newInstance(new Object[]{target});
            } else {
                proxy = loader.loadClass(targetClassName + "Proxy").getConstructor(new Class[]{Object.class}).newInstance(new Object[]{target});
            }

            return proxy;
        }
    }

    public static String h() {
        return ProxyInvoker.class.getSimpleName().toLowerCase().substring(2, 7);
    }

    public static void main(String[] args) {
        System.out.println(h());
    }

}

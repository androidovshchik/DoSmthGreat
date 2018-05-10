# rxjava
-dontwarn rx.internal.util.**
-keep class rx.schedulers.Schedulers { public static <methods>; }
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

# butterknife
-dontwarn butterknife.internal.**
-keep class butterknife.** { *; }
-keep class **_ViewBinding { *; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
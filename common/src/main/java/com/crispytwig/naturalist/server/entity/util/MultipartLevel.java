package com.crispytwig.naturalist.server.entity.util;

import com.crispytwig.naturalist.server.entity.mob.WhalePart;
import org.jetbrains.annotations.Nullable;

public interface MultipartLevel {
    void naturalist$addWhalePart(WhalePart part);

    void naturalist$removeWhalePart(WhalePart part);

    @Nullable
    WhalePart naturalist$getWhalePart(int id);
}

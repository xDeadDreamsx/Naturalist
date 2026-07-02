package com.crispytwig.naturalist.server.entity.base;

public interface VariantAnimal {
    String VARIANT_TAG = "Variant";

    int getVariant();

    void setVariant(int variant);

    String[] getVariantNames();

    default int getVariantCount() {
        return this.getVariantNames().length;
    }

    default String getVariantName() {
        return this.getVariantNames()[Math.floorMod(this.getVariant(), this.getVariantCount())];
    }
}

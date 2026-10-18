package net.lingmu.thaumictinkerer;

/** Whole-orb transfers preserve both stored XP and merged orb counts at capacity. */
public final class ExperienceTransfer {
    public static int count(int stored, int capacity, int orbValue, int orbCount) {
        if (stored < 0 || stored >= capacity || orbValue <= 0 || orbCount <= 0) return 0;
        return Math.min(orbCount, (capacity - stored) / orbValue);
    }
    private ExperienceTransfer() {}
}

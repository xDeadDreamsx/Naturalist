from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")


def main() -> None:
    changed = []
    for path in ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        original = text

        # LivingEntity#blockedByShield(LivingEntity) was renamed to
        # blockedByItem(LivingEntity) in 26.2. Naturalist overrides this attacker-side hook
        # (notably Rhino's charge stun), so keeping the old method name silently disables
        # the behavior even though the class still compiles.
        text = text.replace(
            "    @Override\n    protected void blockedByShield(LivingEntity defender) {",
            "    @Override\n    protected void blockedByItem(LivingEntity defender) {",
        )
        text = text.replace(
            "    protected void blockedByShield(LivingEntity defender) {",
            "    @Override\n    protected void blockedByItem(LivingEntity defender) {",
        )

        if text != original:
            path.write_text(text, encoding="utf-8")
            changed.append(str(path))

    print(f"26.2 shield-block parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()

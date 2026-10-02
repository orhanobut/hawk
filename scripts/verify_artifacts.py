"""Inspect the local release publication and consumer documentation without network access."""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
properties = dict(line.split("=", 1) for line in (ROOT / "gradle.properties").read_text().splitlines() if "=" in line)
version = properties["VERSION_NAME"]
base = ROOT / "hawk/build/repository/com/orhanobut/hawk" / version
stem = f"hawk-{version}"
if version.endswith("-SNAPSHOT"):
    snapshot = ET.parse(base / "maven-metadata.xml").getroot()
    artifact_version = next(
        item.findtext("value") for item in snapshot.findall("versioning/snapshotVersions/snapshotVersion")
        if item.findtext("extension") == "pom" and item.find("classifier") is None
    )
    stem = f"hawk-{artifact_version}"
ns = {"m": "http://maven.apache.org/POM/4.0.0"}
pom = ET.parse(base / f"{stem}.pom").getroot()
assert pom.findtext("m:groupId", namespaces=ns) == "com.orhanobut"
assert pom.findtext("m:artifactId", namespaces=ns) == "hawk"
assert pom.findtext("m:version", namespaces=ns) == version
assert pom.findtext("m:packaging", namespaces=ns) == "aar"
for field in ("name", "description", "url", "licenses", "developers", "scm"):
    assert pom.find(f"m:{field}", ns) is not None, field
catalog = (ROOT / "gradle/libs.versions.toml").read_text()
def catalog_version(name):
    return re.search(rf'^{re.escape(name)} = "([^"]+)"', catalog, re.M)[1]
expected = {
    ("com.facebook.conceal", "conceal"): catalog_version("conceal"),
    ("com.google.code.gson", "gson"): catalog_version("gson"),
    ("org.jetbrains.kotlin", "kotlin-stdlib"): catalog_version("kotlin"),
}
actual = {}
for dependency in pom.findall("m:dependencies/m:dependency", ns):
    key = (dependency.findtext("m:groupId", namespaces=ns), dependency.findtext("m:artifactId", namespaces=ns))
    actual[key] = dependency.findtext("m:version", namespaces=ns)
    assert dependency.findtext("m:scope", namespaces=ns) in ("compile", "runtime")
assert actual == expected, actual
metadata = json.loads((base / f"{stem}.module").read_text())
for variant in metadata["variants"]:
    if "ApiElements" in variant["name"] or "RuntimeElements" in variant["name"]:
        deps = {(d["group"], d["module"]): d["version"]["requires"] for d in variant.get("dependencies", [])}
        assert deps == expected, (variant["name"], deps)
with ZipFile(base / f"{stem}.aar") as aar:
    assert "classes.jar" in aar.namelist()
    assert "proguard.txt" in aar.namelist()
    assert not any(name.endswith(".so") for name in aar.namelist())  # Conceal stays an external dependency.
    manifest = ET.fromstring(aar.read("AndroidManifest.xml"))
    minimum = manifest.find("uses-sdk").get("{http://schemas.android.com/apk/res/android}minSdkVersion")
    assert minimum == catalog_version("min-sdk"), minimum
with ZipFile(base / f"{stem}-sources.jar") as sources:
    assert any(name.endswith("Hawk.kt") for name in sources.namelist())
    assert not any(name.endswith(".java") for name in sources.namelist())
with ZipFile(base / f"{stem}-javadoc.jar") as docs:
    assert any(name.endswith("index.html") for name in docs.namelist())
readme = (ROOT / "README.md").read_text()
assert readme.count("```") % 2 == 0
assert "com.orhanobut:hawk:2.0.1" in readme
assert not any(word in readme.lower() for word in ("travis", "jcenter", "bintray", "ossrh"))
for link in re.findall(r'\]\(([^)]+)\)|src="([^"]+)"', readme):
    target = next(part for part in link if part)
    if not target.startswith(("https://", "http://", "#")):
        assert (ROOT / target).is_file(), target
print(f"Verified {stem}: AAR, sources, HTML documentation, POM, module metadata and README links.")
print("Direct consumer dependencies:", actual)

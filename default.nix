{ pkgs ? import ./nix/pkgs.nix }:

with pkgs.lib;

let
  inherit (pkgs) sbt makeWrapper jdk12_headless;
  mainClass = "me.zanini.froniussolar.Boot";
in sbt.mkDerivation rec {
  pname = "fronius-solar-exporter";
  version = "0.0.1";

  depsSha256 = "19gl5n0kiabr8vkda8ppfzmyrya4393jfdijq7yq2ksp6a391gwj";

  nativeBuildInputs = [ makeWrapper ];

  src = sourceByRegex ./. [
    "^project$"
    "^project/.*$"
    "^src$"
    "^src/.*$"
    "^build.sbt$"
  ];

  buildPhase = ''
    sbt stage
  '';

  installPhase = ''
    mkdir -p $out/{bin,lib}
    cp -ar target/universal/stage/lib $out/lib/${pname}

    makeWrapper ${jdk12_headless}/bin/java $out/bin/${pname} \
      --add-flags "-cp '$out/lib/${pname}/*' ${escapeShellArg mainClass}"
  '';
}

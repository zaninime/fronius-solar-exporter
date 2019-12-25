args@{ pkgs ? import ./nix/pkgs.nix }:

with pkgs.lib;

let
  inherit (pkgs) dockerTools stdenv busybox writeScript skopeo su-exec;
  app = import ./. args;

  imageName = "zaninime/${app.pname}";
  imageTag = app.version;
  user = "appuser";

  containerImage = let
    config = let
      entrypoint = writeScript "${app.pname}-launcher" ''
        #!${stdenv.shell}
        set -euo pipefail

        if [ "$1" = '${app.pname}' ]; then
          exec ${su-exec}/bin/su-exec ${user}:${user} ${app}/bin/${app.pname}
        else
          exec "$@"
        fi
      '';
    in {
      Entrypoint = [ entrypoint ];
      Cmd = [ app.pname ];
      ExposedPorts = { "8079/tcp" = { }; };
    };

    appLayers = dockerTools.buildLayeredImage {
      name = "${imageName}-base";
      tag = imageTag;
      contents = [ busybox ];
      inherit config;
    };
  in dockerTools.buildImage {
    name = imageName;
    tag = imageTag;
    fromImage = appLayers;
    diskSize = 4096;

    inherit config;

    runAsRoot = ''
      #!${stdenv.shell}
      set -euo pipefail
      ${dockerTools.shadowSetup}
      set -x

      groupadd -g 999 ${user}
      useradd -d /data -g ${user} -M -u 999 ${user}
    '';
  };
in rec {
  inherit containerImage;
  pushScript = writeScript "${app.pname}-push-container-image-${imageTag}" ''
    #!${stdenv.shell}
    set -euo pipefail
    readonly imageUri="docker.io/${containerImage.imageName}:${containerImage.imageTag}"
    echo "Pushing $imageUri"
    exec "${skopeo}/bin/skopeo" copy "docker-archive:${containerImage}" "docker://$imageUri" "$@"
  '';

  push = mkShell {
    shellHook = ''
      exec ${pushScript}
    '';
  };
}

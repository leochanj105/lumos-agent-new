{ pkgs ? import <nixpkgs> {}
, mvnHash
}:

let
  inherit (pkgs)
    lib
    maven
    jdk8
    aspectj;
in
maven.buildMavenPackage {
  pname = "lumos-agent";
  version = "0.0.1";

  src = ./.;

  inherit mvnHash;

  mvnParameters = lib.escapeShellArgs [
    "-T"
    "64"
    "clean"
    "install"
    "-U"
  ];

  nativeBuildInputs = [
    jdk8
    aspectj
  ];

  mvnJdk = jdk8;

  postInstall = ''
    mv target $out
  '';
}

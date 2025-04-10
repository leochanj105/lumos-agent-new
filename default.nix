{ pkgs ? import <nixpkgs> {} }:

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

  mvnHash = "sha256-Bwau0KzTzEVBmLNjmziumLwzrgMtHw0uwxILIu4qJZU=";

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

"use client";

import { useState, useEffect } from "react";
import { Card } from "@/components/ui/card";
import { CardContent } from "@/components/ui/cardContent";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function ProjectManager() {
    const [title, setTitle] = useState("");
    const [inScope, setInScope] = useState("");
    const [outScope, setOutScope] = useState("");
    const [projects, setProjects] = useState([]);
    const [assets, setAssets] = useState([]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const response = await fetch("/api/createProject", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ title, inScope: inScope.split(","), outScope: outScope.split(",") })
        });
        const data = await response.json();
        setProjects([...projects, data.projectID]);
    };

    useEffect(() => {
        const fetchAssets = async () => {
            const response = await fetch("/api/getAsset", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ projectID: projects })
            });
            const data = await response.json();
            setAssets(data.assets);
        };
        if (projects.length) fetchAssets();
    }, [projects]);

    return (
        <div className="p-4 space-y-4">
            <Card>
                <CardContent>
                    <form onSubmit={handleSubmit} className="space-y-2">
                        <Input placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
                        <Input placeholder="In Scope (comma-separated regex)" value={inScope} onChange={(e) => setInScope(e.target.value)} required />
                        <Input placeholder="Out Scope (comma-separated regex)" value={outScope} onChange={(e) => setOutScope(e.target.value)} required />
                        <Button type="submit">Create Project</Button>
                    </form>
                </CardContent>
            </Card>
            <Card>
                <CardContent>
                    <table className="w-full border-collapse border border-gray-300">
                        <thead>
                            <tr className="bg-gray-100">
                                <th className="border p-2">Asset</th>
                                <th className="border p-2">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {assets.map((asset, index) => (
                                <tr key={index} className="border">
                                    <td className="border p-2">{asset.name}</td>
                                    <td className="border p-2">{asset.details}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </CardContent>
            </Card>
        </div>
    );
}

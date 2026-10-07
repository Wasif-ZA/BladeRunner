"use client";
import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { FiPower, FiActivity, FiCpu, FiServer, FiUnlock, FiLock, FiChevronsUp, FiChevronUp, FiXOctagon } from "react-icons/fi";

type SystemState = 'STARTED' | 'CONNECTED' | 'STOPPED' | 'SLOW_FORWARD' | 'FULL_SPEED' | 'DOORS_OPEN';

const CodeDemo: React.FC = () => {
    const [state, setState] = useState<SystemState>('STARTED');
    const [logs, setLogs] = useState<string[]>([]);
    const [isEspConnected, setIsEspConnected] = useState(false);
    const [ledState, setLedState] = useState<[boolean, boolean, boolean, boolean]>([false, false, false, false]); // 4 LEDs
    const terminalRef = useRef<HTMLDivElement>(null);

    const addLog = (message: string, type: 'info' | 'error' | 'success' | 'tx' | 'rx' = 'info') => {
        const timestamp = new Date().toLocaleTimeString('en-US', { hour12: false });
        const prefix = type === 'tx' ? '>>' : type === 'rx' ? '<<' : '::';
        setLogs(prev => [...prev, `[${timestamp}] ${prefix} ${message}`]);
    };

    // Auto-scroll logic
    useEffect(() => {
        if (terminalRef.current) {
            terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
        }
    }, [logs]);

    // LED Logic Simulator (Based on CCP.java)
    useEffect(() => {
        const resetLeds = () => setLedState([false, false, false, false]);

        // Simple simulation of the Java 'updateLEDState' method
        switch (state) {
            case 'STARTED':
                // Flash LED 0 (Simulated by just turning it on for demo clarity)
                setLedState([true, false, false, false]);
                break;
            case 'CONNECTED':
                // Flash LED 1
                setLedState([false, true, false, false]);
                break;
            case 'STOPPED':
            case 'DOORS_OPEN':
                // LED 0 Steady ON
                setLedState([true, false, false, false]);
                break;
            case 'FULL_SPEED':
                // LED 2 Steady ON
                setLedState([false, false, true, false]);
                break;
            case 'SLOW_FORWARD':
                // LED 2 Flashing (Simulated)
                const interval = setInterval(() => {
                    setLedState(prev => [false, false, !prev[2], false]);
                }, 500);
                return () => clearInterval(interval);
        }
    }, [state]);

    // Heartbeat Simulator
    useEffect(() => {
        if (state !== 'STARTED') {
            const interval = setInterval(() => {
                addLog("HB_PING: { \"msg\": \"STRQ\", \"id\": \"BR12\" }", 'tx');
            }, 2000);
            return () => clearInterval(interval);
        }
    }, [state]);

    const handleConnect = () => {
        addLog("Initializing CCP Kernel v2.0.4...", 'info');
        setTimeout(() => addLog("Loading StateManager...", 'info'), 400);
        setTimeout(() => addLog("Binding UDP Socket :3000...", 'info'), 800);
        setTimeout(() => {
            addLog("TX: { \"type\": \"ccp\", \"msg\": \"CCIN\" } -> MCP", 'tx');
            setTimeout(() => {
                addLog("RX: { \"msg\": \"AKIN\" } <- MCP", 'rx');
                setState('CONNECTED');
                setIsEspConnected(true);
                addLog("System Link Established. Waiting for commands.", 'success');
            }, 500);
        }, 1500);
    };

    const handleAction = (action: string) => {
        if (state === 'STARTED') {
            addLog("ERR: Command rejected. System offline.", 'error');
            return;
        }

        addLog(`RX: EXEC "${action}"`, 'rx');

        switch (action) {
            case 'FFASTC':
                setState('FULL_SPEED');
                addLog("State -> FULL_SPEED. Motor Output: 100%", 'info');
                break;
            case 'FSLOWC':
                setState('SLOW_FORWARD');
                addLog("State -> SLOW_FORWARD. Motor Output: 40%", 'info');
                addLog("Sensors: Scanning for Photodiode Alignment...", 'info');
                break;
            case 'STOPC':
                setState('STOPPED');
                addLog("State -> STOPPED. Doors: LOCKED", 'info');
                break;
            case 'STOPO':
                if (state !== 'STOPPED' && state !== 'DOORS_OPEN') {
                    addLog("WARN: Decelerating before door actuation...", 'info');
                }
                setState('DOORS_OPEN');
                addLog("State -> STOPPED. Doors: OPENING", 'info');
                break;
        }

        // Simulate forwarding to hardware
        setTimeout(() => {
            addLog(`UDP -> ESP32: { "act": "${action}" }`, 'tx');
        }, 100);
    };

    return (
        <div className="bg-neutral-950 border border-cyan-900/50 rounded-xl overflow-hidden shadow-[0_0_50px_-20px_rgba(6,182,212,0.3)] font-mono text-sm max-w-6xl mx-auto my-12">

            {/* Top Bar / Status Header */}
            <div className="bg-black border-b border-cyan-900/30 p-4 flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <div className="flex gap-1.5">
                        <div className="w-3 h-3 rounded-full bg-red-500/20 border border-red-500/50" />
                        <div className="w-3 h-3 rounded-full bg-yellow-500/20 border border-yellow-500/50" />
                        <div className="w-3 h-3 rounded-full bg-green-500/20 border border-green-500/50" />
                    </div>
                    <span className="text-cyan-500 font-bold tracking-widest uppercase text-xs">
                        CCP // TERMINAL_ACCESS
                    </span>
                </div>
                <div className="flex items-center gap-6 text-xs">
                    <StatusIndicator label="UDP LINK" active={isEspConnected} icon={FiServer} />
                    <StatusIndicator label="ESP32" active={isEspConnected} icon={FiCpu} />
                    <div className="px-3 py-1 bg-cyan-900/20 border border-cyan-500/30 text-cyan-400 rounded">
                        {state}
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[500px]">

                {/* Left Panel: Telemetry & Visuals */}
                <div className="lg:col-span-5 bg-black/50 border-r border-cyan-900/30 p-6 flex flex-col gap-6 relative overflow-hidden">
                    {/* Background Grid Effect */}
                    <div className="absolute inset-0 bg-[linear-gradient(rgba(6,182,212,0.02)_1px,transparent_1px),linear-gradient(90deg,rgba(6,182,212,0.02)_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none" />

                    {/* Telemetry Module */}
                    <div className="relative z-10 space-y-6">

                        {/* Speedometer */}
                        <div className="bg-neutral-900/80 border border-white/10 p-4 rounded-lg">
                            <h4 className="text-gray-500 text-[10px] uppercase tracking-widest mb-3 flex justify-between">
                                <span>Motor Output</span>
                                <span>{state === 'FULL_SPEED' ? '100%' : state === 'SLOW_FORWARD' ? '40%' : '0%'}</span>
                            </h4>
                            <div className="h-4 bg-black rounded-sm overflow-hidden border border-white/5 relative">
                                {/* Tick marks */}
                                <div className="absolute inset-0 flex justify-between px-1">
                                    {[...Array(10)].map((_, i) => <div key={i} className="w-px h-full bg-white/10" />)}
                                </div>
                                <motion.div
                                    className="h-full bg-gradient-to-r from-cyan-900 to-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.5)]"
                                    animate={{ width: state === 'FULL_SPEED' ? '100%' : state === 'SLOW_FORWARD' ? '40%' : '0%' }}
                                    transition={{ type: "spring", stiffness: 50 }}
                                />
                            </div>
                        </div>

                        {/* Door Visualizer */}
                        <div className="bg-neutral-900/80 border border-white/10 p-4 rounded-lg h-32 flex items-center justify-center relative overflow-hidden">
                            <div className="absolute top-2 left-3 text-[10px] text-gray-500 uppercase tracking-widest">Door Actuators</div>

                            <div className="flex items-center gap-1 relative">
                                {/* Left Door */}
                                <motion.div
                                    animate={{ x: state === 'DOORS_OPEN' ? -20 : 0 }}
                                    className="w-12 h-20 bg-neutral-700 border-r-2 border-black flex items-center justify-center relative z-10"
                                >
                                    <div className="w-8 h-12 border border-white/10 bg-black/20" />
                                </motion.div>

                                {/* Right Door */}
                                <motion.div
                                    animate={{ x: state === 'DOORS_OPEN' ? 20 : 0 }}
                                    className="w-12 h-20 bg-neutral-700 border-l-2 border-black flex items-center justify-center relative z-10"
                                >
                                    <div className="w-8 h-12 border border-white/10 bg-black/20" />
                                </motion.div>

                                {/* Warning Strip behind doors */}
                                <div className="absolute inset-0 flex items-center justify-center -z-0">
                                    <div className="w-full h-full bg-[repeating-linear-gradient(45deg,transparent,transparent_5px,#eab308_5px,#eab308_10px)] opacity-20" />
                                </div>
                            </div>

                            <div className={`absolute bottom-2 text-xs font-bold ${state === 'DOORS_OPEN' ? 'text-green-500' : 'text-red-500'}`}>
                                {state === 'DOORS_OPEN' ? 'UNLOCKED' : 'LOCKED'}
                            </div>
                        </div>

                        {/* Hardware LEDs (Simulated from CCP.java) */}
                        <div className="bg-neutral-900/80 border border-white/10 p-4 rounded-lg">
                            <h4 className="text-gray-500 text-[10px] uppercase tracking-widest mb-3">Diagnostic LEDs (ESP32)</h4>
                            <div className="flex justify-between px-4">
                                {ledState.map((isOn, i) => (
                                    <div key={i} className="flex flex-col items-center gap-2">
                                        <motion.div
                                            animate={{
                                                backgroundColor: isOn ? '#06b6d4' : '#171717',
                                                boxShadow: isOn ? '0 0 10px #06b6d4' : 'none'
                                            }}
                                            className="w-4 h-4 rounded-full border border-gray-700"
                                        />
                                        <span className="text-[10px] text-gray-600">L{i}</span>
                                    </div>
                                ))}
                            </div>
                        </div>

                    </div>
                </div>

                {/* Right Panel: Controls & Terminal */}
                <div className="lg:col-span-7 flex flex-col">

                    {/* Control Grid */}
                    <div className="p-6 grid grid-cols-2 gap-4 bg-neutral-900/30 border-b border-cyan-900/30">
                        {state === 'STARTED' ? (
                            <button
                                onClick={handleConnect}
                                className="col-span-2 py-4 bg-cyan-600/20 border border-cyan-500 text-cyan-400 font-bold hover:bg-cyan-600/40 hover:shadow-[0_0_20px_rgba(6,182,212,0.2)] transition-all uppercase tracking-wider flex items-center justify-center gap-2"
                            >
                                <FiPower /> Initialize System
                            </button>
                        ) : (
                            <>
                                <ControlButton onClick={() => handleAction('FFASTC')} label="Fast Forward" sub="100% Pwr" icon={FiChevronsUp} color="cyan" />
                                <ControlButton onClick={() => handleAction('FSLOWC')} label="Slow Forward" sub="Align Mode" icon={FiChevronUp} color="blue" />
                                <ControlButton onClick={() => handleAction('STOPC')} label="Stop & Lock" sub="Emergency" icon={FiXOctagon} color="red" />
                                <ControlButton onClick={() => handleAction('STOPO')} label="Stop & Open" sub="Station" icon={FiUnlock} color="green" />
                            </>
                        )}
                    </div>

                    {/* Terminal Window */}
                    <div className="flex-grow bg-black p-4 font-mono text-xs overflow-hidden flex flex-col relative">
                        <div className="absolute top-2 right-4 text-gray-700 text-[10px] select-none">build 2026.01.24_rc</div>
                        <div
                            ref={terminalRef}
                            className="flex-grow overflow-y-auto space-y-1 pr-2 scrollbar-thin scrollbar-thumb-cyan-900 scrollbar-track-transparent"
                        >
                            {logs.length === 0 && (
                                <div className="h-full flex items-center justify-center text-gray-800 italic">
                                    Waiting for initialization signal...
                                </div>
                            )}
                            {logs.map((log, i) => {
                                // Basic syntax highlighting for logs
                                const isTx = log.includes('>>');
                                const isRx = log.includes('<<');
                                const isErr = log.includes('ERR');
                                const isSuccess = log.includes('Success') || log.includes('Established');

                                let color = "text-gray-400";
                                if (isTx) color = "text-blue-400";
                                if (isRx) color = "text-purple-400";
                                if (isErr) color = "text-red-500";
                                if (isSuccess) color = "text-green-400";

                                return (
                                    <div key={i} className={`${color} break-all hover:bg-white/5 px-1 rounded`}>
                                        {log}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>

            </div>
        </div>
    );
};

// --- Helper Components ---

const StatusIndicator: React.FC<{ label: string; active: boolean; icon: React.ElementType }> = ({ label, active, icon: Icon }) => (
    <div className={`flex items-center gap-2 ${active ? 'text-green-500' : 'text-gray-600'}`}>
        <Icon className={active ? 'animate-pulse' : ''} />
        <span className="font-bold">{label}</span>
    </div>
);

const ControlButton: React.FC<{ onClick: () => void; label: string; sub: string; icon: React.ElementType; color: string }> = ({ onClick, label, sub, icon: Icon, color }) => {
    // Map color strings to Tailwind classes
    const colors: Record<string, string> = {
        cyan: "border-cyan-700 hover:bg-cyan-900/30 text-cyan-400",
        blue: "border-blue-700 hover:bg-blue-900/30 text-blue-400",
        red: "border-red-700 hover:bg-red-900/30 text-red-400",
        green: "border-green-700 hover:bg-green-900/30 text-green-400",
    };

    return (
        <button
            onClick={onClick}
            className={`border bg-black/40 p-3 flex items-center gap-3 transition-all hover:translate-y-[-2px] hover:shadow-lg ${colors[color] || colors.cyan}`}
        >
            <div className={`p-2 rounded bg-black/50 border border-white/10`}>
                <Icon size={20} />
            </div>
            <div className="text-left">
                <div className="font-bold text-xs uppercase tracking-wider">{label}</div>
                <div className="text-[10px] opacity-60">{sub}</div>
            </div>
        </button>
    );
};

export default CodeDemo;
